import {
  collection, doc, getDocs, getDoc,
  setDoc, deleteDoc, serverTimestamp, query, orderBy,
  writeBatch,
} from 'firebase/firestore'
import type { CollectionReference, DocumentData } from 'firebase/firestore'
import { db } from './firebase'
import type { ExaminationRecord, ItemMaster } from './types'
import { recalcRecordsForMaster, referenceRangeChanged } from './lib/recalcAbnormal'

const recordsRef = (uid: string) =>
  collection(db, 'users', uid, 'records')

const mastersRef = (uid: string) =>
  collection(db, 'users', uid, 'itemMasters')

// WriteBatch は1回あたり最大500件までしか操作できないため、500件ごとに分割してコミットする。
const FIRESTORE_BATCH_LIMIT = 500

// ── 診断記録 ────────────────────────────────────────────

export async function fetchRecords(uid: string): Promise<ExaminationRecord[]> {
  const q = query(recordsRef(uid), orderBy('date', 'desc'))
  const snap = await getDocs(q)
  return snap.docs.map(d => ({ id: d.id, ...d.data() } as ExaminationRecord))
}

export async function fetchRecord(uid: string, recordId: string): Promise<ExaminationRecord | null> {
  const snap = await getDoc(doc(db, 'users', uid, 'records', recordId))
  if (!snap.exists()) return null
  return { id: snap.id, ...snap.data() } as ExaminationRecord
}

export async function saveRecord(
  uid: string,
  record: Omit<ExaminationRecord, 'id' | 'createdAt'>,
  existingId?: string,
): Promise<string> {
  // Issue #49: 新規記録のドキュメントIDはグローバルに一意なUUIDにする。
  // 従来の Date.now()（13桁ミリ秒）は同時刻に近い操作で衝突しうる採番ではないが、
  // Android側もRoomのローカル連番からUUID（remoteId）へ切り替えたため、プラットフォーム間で
  // 採番方式を揃える（既存ドキュメントの一括書き換えは行わない。数値IDとUUIDは共存する）。
  // createdAt の挙動（編集時にserverTimestamp()で上書きする点）は Issue #75 のスコープのため変更しない。
  const id = existingId ?? crypto.randomUUID()
  await setDoc(doc(db, 'users', uid, 'records', id), {
    ...record,
    createdAt: serverTimestamp(),
  })
  return id
}

export async function deleteRecord(uid: string, recordId: string): Promise<void> {
  await deleteDoc(doc(db, 'users', uid, 'records', recordId))
}

// ── 項目マスター ─────────────────────────────────────────

export async function fetchMasters(uid: string): Promise<ItemMaster[]> {
  const snap = await getDocs(query(mastersRef(uid), orderBy('__name__')))
  return snap.docs.map(d => ({ itemName: d.id, ...d.data() } as ItemMaster))
}

/**
 * 項目マスターを保存する。
 * 基準値（referenceMin / referenceMax）が実際に変化した場合のみ、
 * Android の HealthRepository.upsertMaster()（Issue #8 / PR #11）と同じ判定で
 * 該当項目名を含む既存記録を再計算し、Firestore 上の
 * items[].referenceMin / referenceMax / isAbnormal を更新する
 * （カテゴリ変更・お気に入りトグルのみでは記録を更新しない＝不要な全件書き込みを避ける）。
 *
 * Issue #50 の前提: 再計算は Web クライアント側で全記録を取得して走査する方式
 * （Cloud Functions への移行は行わない）。判定ロジック自体は ./lib/recalcAbnormal.ts に
 * 純関数として切り出してあるため、将来 Cloud Functions 側に寄せる場合もそのまま流用できる。
 */
export async function saveMaster(uid: string, master: ItemMaster): Promise<void> {
  const { itemName, ...rest } = master
  const masterRef = doc(db, 'users', uid, 'itemMasters', itemName)
  const previousSnap = await getDoc(masterRef)
  const previous = previousSnap.exists()
    ? (previousSnap.data() as Pick<ItemMaster, 'referenceMin' | 'referenceMax'>)
    : null

  await setDoc(masterRef, rest)

  if (!referenceRangeChanged(previous, master)) return

  const records = await fetchRecords(uid)
  const updates = recalcRecordsForMaster(records, itemName, master.referenceMin, master.referenceMax)
  if (updates.length === 0) return

  for (let i = 0; i < updates.length; i += FIRESTORE_BATCH_LIMIT) {
    const batch = writeBatch(db)
    for (const update of updates.slice(i, i + FIRESTORE_BATCH_LIMIT)) {
      batch.update(doc(db, 'users', uid, 'records', update.recordId), { items: update.items })
    }
    await batch.commit()
  }
}

export async function deleteMaster(uid: string, itemName: string): Promise<void> {
  await deleteDoc(doc(db, 'users', uid, 'itemMasters', itemName))
}

// ── アカウント削除（Issue #34） ────────────────────────────

/**
 * コレクション内の全ドキュメントを削除する。
 * WriteBatch は1回あたり最大500件までしか操作できないため、500件ごとに分割してコミットする。
 */
async function deleteAllDocsIn(ref: CollectionReference<DocumentData>): Promise<void> {
  const snap = await getDocs(ref)
  if (snap.empty) return
  const docs = snap.docs
  for (let i = 0; i < docs.length; i += FIRESTORE_BATCH_LIMIT) {
    const batch = writeBatch(db)
    for (const d of docs.slice(i, i + FIRESTORE_BATCH_LIMIT)) {
      batch.delete(d.ref)
    }
    await batch.commit()
  }
}

/**
 * アカウント削除機能用。users/{uid} 配下の records・itemMasters コレクションの
 * 全ドキュメントを削除する。冪等: 既にデータが無い状態で呼んでも例外を投げない。
 */
export async function deleteAllUserData(uid: string): Promise<void> {
  await deleteAllDocsIn(recordsRef(uid))
  await deleteAllDocsIn(mastersRef(uid))
}
