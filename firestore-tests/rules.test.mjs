/**
 * firestore.rules のユニットテスト。
 *
 * 実行方法（リポジトリルートから）:
 *   npm --prefix firestore-tests install
 *   firebase emulators:exec --only firestore "npm --prefix firestore-tests test"
 *
 * 検証方針: 認証済みユーザー本人のみが users/{uid} 配下を読み書きでき、
 * 他人・未認証は一切アクセスできないこと。
 */
import { readFileSync } from 'node:fs';
import { after, before, describe, it } from 'node:test';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment,
} from '@firebase/rules-unit-testing';
import { doc, getDoc, setDoc, collection, getDocs } from 'firebase/firestore';

const ALICE = 'alice-uid';
const BOB = 'bob-uid';

let testEnv;

before(async () => {
  testEnv = await initializeTestEnvironment({
    projectId: 'health-checkup-manager-rules-test',
    firestore: {
      rules: readFileSync(new URL('../firestore.rules', import.meta.url), 'utf8'),
    },
  });
});

after(async () => {
  await testEnv?.cleanup();
});

describe('firestore.rules — 診断記録 users/{uid}/records', () => {
  it('本人は自分の記録を書き込める', async () => {
    const db = testEnv.authenticatedContext(ALICE).firestore();
    await assertSucceeds(
      setDoc(doc(db, `users/${ALICE}/records/1`), {
        date: '2026-04-01',
        facility: '六創堂クリニック',
        createdAt: 1743465600000,
        items: [],
      }),
    );
  });

  it('本人は自分の記録を読める', async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), `users/${ALICE}/records/2`), { date: '2026-04-02' });
    });
    const db = testEnv.authenticatedContext(ALICE).firestore();
    await assertSucceeds(getDoc(doc(db, `users/${ALICE}/records/2`)));
  });

  it('他人の記録は読めない', async () => {
    await testEnv.withSecurityRulesDisabled(async (ctx) => {
      await setDoc(doc(ctx.firestore(), `users/${ALICE}/records/3`), { date: '2026-04-03' });
    });
    const db = testEnv.authenticatedContext(BOB).firestore();
    await assertFails(getDoc(doc(db, `users/${ALICE}/records/3`)));
  });

  it('他人の記録は書き込めない', async () => {
    const db = testEnv.authenticatedContext(BOB).firestore();
    await assertFails(setDoc(doc(db, `users/${ALICE}/records/4`), { date: '2026-04-04' }));
  });

  it('未認証は読めない', async () => {
    const db = testEnv.unauthenticatedContext().firestore();
    await assertFails(getDoc(doc(db, `users/${ALICE}/records/2`)));
  });

  it('未認証は書き込めない', async () => {
    const db = testEnv.unauthenticatedContext().firestore();
    await assertFails(setDoc(doc(db, `users/${ALICE}/records/5`), { date: '2026-04-05' }));
  });

  it('他人のコレクション一覧は取得できない', async () => {
    const db = testEnv.authenticatedContext(BOB).firestore();
    await assertFails(getDocs(collection(db, `users/${ALICE}/records`)));
  });
});

describe('firestore.rules — 項目マスター users/{uid}/itemMasters', () => {
  it('本人は自分のマスターを書き込める', async () => {
    const db = testEnv.authenticatedContext(ALICE).firestore();
    await assertSucceeds(
      setDoc(doc(db, `users/${ALICE}/itemMasters/血糖値`), {
        unit: 'mg/dL',
        referenceMin: 70,
        referenceMax: 109,
        category: 'blood',
        isFavorite: true,
        favoritedAt: 1743465600000,
      }),
    );
  });

  it('他人のマスターは書き込めない', async () => {
    const db = testEnv.authenticatedContext(BOB).firestore();
    await assertFails(setDoc(doc(db, `users/${ALICE}/itemMasters/血糖値`), { unit: 'mg/dL' }));
  });
});

describe('firestore.rules — 想定外パスは既定で拒否', () => {
  it('users 配下でないコレクションは本人でも書き込めない', async () => {
    const db = testEnv.authenticatedContext(ALICE).firestore();
    await assertFails(setDoc(doc(db, 'adminSettings/global'), { foo: 'bar' }));
  });

  it('users ドキュメント直下も uid が一致しなければ拒否', async () => {
    const db = testEnv.authenticatedContext(BOB).firestore();
    await assertFails(setDoc(doc(db, `users/${ALICE}`), { foo: 'bar' }));
  });
});
