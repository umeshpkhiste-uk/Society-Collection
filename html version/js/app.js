import { firebaseConfig } from './firebase-config.js';
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js';
import {
  getFirestore, collection, addDoc, onSnapshot,
  orderBy, query, runTransaction, doc, serverTimestamp
} from 'https://www.gstatic.com/firebasejs/10.12.2/firebase-firestore.js';

// ---------- Firebase setup ----------
let db = null;
let backendReady = false;

function isConfigFilled() {
  return firebaseConfig.apiKey && !firebaseConfig.apiKey.startsWith('YOUR_');
}

if (isConfigFilled()) {
  try {
    const app = initializeApp(firebaseConfig);
    db = getFirestore(app);
    backendReady = true;
  } catch (e) {
    console.error('Firebase init failed', e);
  }
}

const banner = document.getElementById('statusBanner');
if (!backendReady) {
  banner.textContent = 'Backend not configured yet — fill in js/firebase-config.js with your Firebase project details. Nothing will save until then.';
  banner.classList.add('show', 'error');
}

// ---------- State ----------
let FLATS = [];
let receipts = [];
let selectedFlat = null;

async function loadFlats() {
  const res = await fetch('assets/flats.json');
  FLATS = await res.json();
}

function listenToReceipts() {
  if (!backendReady) return;
  const q = query(collection(db, 'receipts'), orderBy('receiptNo', 'desc'));
  onSnapshot(q, (snapshot) => {
    receipts = snapshot.docs.map(d => ({ id: d.id, ...d.data() }));
    renderHome();
    renderReports();
  }, (err) => {
    console.error(err);
    banner.textContent = 'Could not connect to the backend: ' + err.message;
    banner.classList.add('show', 'error');
  });
}

function paidSet() { return new Set(receipts.map(r => r.flatNumber)); }

// ---------- Home screen ----------
function renderHome() {
  const query_ = (document.getElementById('homeSearch').value || '').toLowerCase();
  const paid = paidSet();
  const filtered = FLATS.filter(f =>
    !query_ || f.number.includes(query_) || f.name.toLowerCase().includes(query_)
  );
  document.getElementById('sumPaid').textContent = paid.size;
  document.getElementById('sumPending').textContent = FLATS.length - paid.size;
  document.getElementById('sumTotal').textContent = FLATS.length;

  const groups = {};
  filtered.forEach(f => {
    const floor = f.number.length <= 2 ? 0 : parseInt(f.number.slice(0, -2), 10);
    (groups[floor] = groups[floor] || []).push(f);
  });

  const container = document.getElementById('flatGroups');
  container.innerHTML = '';
  Object.keys(groups).map(Number).sort((a, b) => a - b).forEach(floor => {
    const heading = document.createElement('div');
    heading.className = 'floor-heading';
    heading.textContent = floor === 0 ? 'Ground / Other' : 'Floor ' + floor;
    container.appendChild(heading);

    const grid = document.createElement('div');
    grid.className = 'flat-grid';
    groups[floor].forEach(f => {
      const btn = document.createElement('button');
      btn.className = 'flat-btn' + (paid.has(f.number) ? ' is-paid' : '');
      btn.innerHTML = f.number + '<div class="strip"></div>';
      btn.onclick = () => openReceipt(f);
      grid.appendChild(btn);
    });
    container.appendChild(grid);
  });
}

// ---------- Receipt screen ----------
function openReceipt(flat) {
  selectedFlat = flat;
  document.getElementById('rFlatNum').textContent = 'Flat ' + flat.number;
  document.getElementById('rFlatName').textContent = flat.name || '(name not on record)';
  document.getElementById('rFlatWing').textContent = 'Wing ' + flat.wing;
  document.getElementById('fAmount').value = '';
  document.getElementById('fAccountOf').value = 'Annual Maintenance Fund';
  document.getElementById('fMode').value = 'Cash';
  document.getElementById('fRef').value = '';
  document.getElementById('fBank').value = '';
  document.getElementById('fBranch').value = '';
  const today = new Date();
  const dd = String(today.getDate()).padStart(2, '0');
  const mm = String(today.getMonth() + 1).padStart(2, '0');
  document.getElementById('fDate').value = `${dd}/${mm}/${today.getFullYear()}`;
  onModeChange();
  showScreen('receipt');
}

window.onModeChange = function () {
  const mode = document.getElementById('fMode').value;
  const extra = document.getElementById('extraFields');
  extra.style.display = (mode === 'Cheque' || mode === 'NEFT') ? 'block' : 'none';
  document.getElementById('refLabel').textContent = mode === 'Cheque' ? 'Cheque No.' : 'Reference / UTR No.';
};

// Atomically reserves the next receipt number, same scheme as the Android app
// (continues from 2477 to match the physical receipt book's Rec. No. 2478).
async function getNextReceiptNumber() {
  const counterRef = doc(db, 'counters', 'receiptCounter');
  return await runTransaction(db, async (transaction) => {
    const snap = await transaction.get(counterRef);
    const current = snap.exists() ? snap.data().lastNumber : 2477;
    const next = current + 1;
    transaction.set(counterRef, { lastNumber: next });
    return next;
  });
}

window.submitReceipt = async function () {
  if (!backendReady) {
    banner.textContent = 'Backend not configured yet — fill in js/firebase-config.js before saving.';
    banner.classList.add('show', 'error');
    return;
  }
  const amount = parseFloat(document.getElementById('fAmount').value);
  if (!amount || amount <= 0) { alert('Enter a valid amount'); return; }

  const btn = document.getElementById('submitBtn');
  btn.disabled = true;
  btn.textContent = 'Saving...';

  try {
    const receiptNo = await getNextReceiptNumber();
    const receipt = {
      receiptNo,
      flatNumber: selectedFlat.number,
      wing: selectedFlat.wing,
      residentName: selectedFlat.name,
      amount,
      accountOf: document.getElementById('fAccountOf').value,
      paymentMode: document.getElementById('fMode').value,
      chequeOrRefNo: document.getElementById('fRef').value,
      bankName: document.getElementById('fBank').value,
      branch: document.getElementById('fBranch').value,
      receiptDate: document.getElementById('fDate').value,
      enteredBy: 'Committee (Web)',
      createdAt: serverTimestamp()
    };
    await addDoc(collection(db, 'receipts'), receipt);
    showReceiptPaper({ ...receipt, receiptNo });
  } catch (e) {
    console.error(e);
    banner.textContent = 'Could not save the receipt: ' + e.message;
    banner.classList.add('show', 'error');
  } finally {
    btn.disabled = false;
    btn.textContent = 'Save Receipt';
  }
};

function showReceiptPaper(r) {
  const paper = document.getElementById('receiptPaper');
  paper.innerHTML = `
    <div class="letterhead">LIFE REPUBLIC SECTOR R10 / 10th AVENUE<br>UNIVERSE SAHAKARI GRUHARACHNA SANSTHA MARYADIT</div>
    <div class="recno">Receipt No. ${r.receiptNo}</div>
    <div class="rrow"><div class="rl">Received with thanks from</div><div class="rv">${r.residentName || '-'}</div></div>
    <div class="rrow"><div class="rl">Residents of</div><div class="rv">Flat ${r.flatNumber}, Wing ${r.wing}</div></div>
    <div class="rrow"><div class="rl">The sum of Rupees</div><div class="rv">Rs. ${r.amount.toFixed(2)}</div></div>
    <div class="rrow"><div class="rl">On account of</div><div class="rv">${r.accountOf}</div></div>
    <div class="rrow"><div class="rl">Payment via</div><div class="rv">${r.paymentMode}</div></div>
    ${r.chequeOrRefNo ? `<div class="rrow"><div class="rl">Cheque/Ref No.</div><div class="rv">${r.chequeOrRefNo}</div></div>` : ''}
    ${r.bankName ? `<div class="rrow"><div class="rl">Drawn on Bank</div><div class="rv">${r.bankName}</div></div>` : ''}
    <div class="rrow"><div class="rl">Date</div><div class="rv">${r.receiptDate}</div></div>
    <button class="modal-close" onclick="closeModal()">Done</button>
  `;
  document.getElementById('modalBackdrop').classList.add('show');
}

window.closeModal = function () {
  document.getElementById('modalBackdrop').classList.remove('show');
  goHome();
};

// ---------- Reports screen ----------
function renderReports() {
  const q = (document.getElementById('reportSearch').value || '').toLowerCase();
  const dateQ = document.getElementById('reportDate').value || '';
  const filtered = receipts.filter(r =>
    (!q || r.flatNumber.includes(q) || (r.residentName || '').toLowerCase().includes(q) || String(r.receiptNo).includes(q)) &&
    (!dateQ || (r.receiptDate || '').includes(dateQ))
  );
  const total = filtered.reduce((s, r) => s + r.amount, 0);
  document.getElementById('reportCount').textContent = filtered.length + ' receipts';
  document.getElementById('reportTotal').textContent = 'Rs. ' + total.toFixed(2);

  const list = document.getElementById('reportList');
  if (filtered.length === 0) {
    list.innerHTML = '<div class="empty">No receipts found</div>';
    return;
  }
  list.innerHTML = filtered.map(r => `
    <div class="receipt-item">
      <div>
        <div class="flat">Flat ${r.flatNumber} &middot; ${r.residentName || '-'}</div>
        <div class="meta">Receipt #${r.receiptNo} &middot; ${r.receiptDate}</div>
        <div class="meta">${r.paymentMode} &middot; ${r.accountOf}</div>
      </div>
      <div class="amt">Rs. ${r.amount.toFixed(2)}</div>
    </div>
  `).join('');
}

// ---------- Navigation ----------
function showScreen(name) {
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  document.getElementById('screen-' + name).classList.add('active');
}

window.switchTab = function (tab) {
  document.querySelectorAll('nav.tabbar button').forEach(b => b.classList.remove('active'));
  document.querySelector(`nav.tabbar button[data-tab="${tab}"]`).classList.add('active');
  showScreen(tab);
};

window.goHome = function () { switchTab('home'); };

document.getElementById('homeSearch').addEventListener('input', renderHome);
document.getElementById('reportSearch').addEventListener('input', renderReports);
document.getElementById('reportDate').addEventListener('input', renderReports);

// ---------- Boot ----------
(async function init() {
  await loadFlats();
  renderHome();
  listenToReceipts();
})();
