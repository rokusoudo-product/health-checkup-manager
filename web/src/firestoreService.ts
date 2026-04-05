import {
  collection, doc, getDocs, getDoc,
  setDoc, deleteDoc, serverTimestamp, query, orderBy,
} from 'firebase/firestore'
import { db } from './firebase'
import type { ExaminationRecord, ItemMaster } from './types'

const recordsRef = (uid: string) =>
  collection(db, 'users', uid, 'records')

const mastersRef = (uid: string) =>
  collection(db, 'users', uid, 'itemMasters')

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
  const id = existingId ?? String(Date.now())
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

export async function saveMaster(uid: string, master: ItemMaster): Promise<void> {
  const { itemName, ...rest } = master
  await setDoc(doc(db, 'users', uid, 'itemMasters', itemName), rest)
}

export async function deleteMaster(uid: string, itemName: string): Promise<void> {
  await deleteDoc(doc(db, 'users', uid, 'itemMasters', itemName))
}
