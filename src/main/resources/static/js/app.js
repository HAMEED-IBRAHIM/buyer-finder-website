// ─── Card Avatar Colors ───────────────────────────────────────────────────
const AVATAR_COLORS = [
    'linear-gradient(135deg, #3b5bdb, #5c7cfa)',
    'linear-gradient(135deg, #0ca678, #20c997)',
    'linear-gradient(135deg, #7048e8, #9775fa)',
    'linear-gradient(135deg, #e8590c, #fd7e14)',
    'linear-gradient(135deg, #1098ad, #22b8cf)',
    'linear-gradient(135deg, #d6336c, #f06595)',
];

// ─── Loading messages that cycle while AI thinks ──────────────────────────
const LOADING_MSGS = [
    ['Searching for buyers…',       'Scanning leads across the US'],
    ['AI is analyzing markets…',    'Looking at retailers and boutiques'],
    ['Finding contact details…',    'Pulling business profiles'],
    ['Almost done!',                'Compiling your lead list'],
];

// ─── DOM refs ─────────────────────────────────────────────────────────────
const $ = id => document.getElementById(id);

const searchInput  = $('search-input');
const searchBtn    = $('search-btn');
const loginWarning = $('login-warning');
const loginBtn     = $('login-btn');
const userPill     = $('user-pill');
const userAvatar   = $('user-avatar');
const userName     = $('user-name');
const quickTags    = document.querySelectorAll('.tag-chip');

const resultsSection = $('results-section');
const resultsTitle   = $('results-title');
const resultsCount   = $('results-count');
const loadingState   = $('loading-state');
const buyersGrid     = $('buyers-grid');
const newSearchBtn   = $('new-search-btn');
const resultsMeta    = $('results-meta');
const resultsTime    = $('results-time');

const emailModal    = $('email-modal');
const closeModal    = $('close-modal');
const modalBuyerName= $('modal-buyer-name');
const emailTo       = $('email-to');
const emailSubject  = $('email-subject');
const emailBody     = $('email-body');
const sendEmailBtn  = $('send-email-btn');
const emailStatus   = $('email-status');

let isAuthenticated = false;
let loadingMsgTimer = null;

// ─── Toast ────────────────────────────────────────────────────────────────
function showToast(msg, icon = '✨', duration = 3000) {
    const toast = $('toast');
    $('toast-msg').textContent = msg;
    toast.querySelector('.toast-icon').textContent = icon;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), duration);
}

// ─── Auth Check ───────────────────────────────────────────────────────────
fetch('/api/user')
    .then(r => { if (r.ok) return r.json(); throw new Error('unauth'); })
    .then(data => {
        const isDemoMode   = sessionStorage.getItem('demoMode') === 'true';
        const isGoogleAuth = data.email && data.email !== 'demo@decorleads.com';

        if (!isGoogleAuth && !isDemoMode) {
            window.location.replace('/login.html');
            return;
        }

        isAuthenticated = true;
        loginBtn?.classList.add('hidden');
        userPill.classList.remove('hidden');
        userAvatar.src = data.picture ||
            `https://ui-avatars.com/api/?name=${encodeURIComponent(data.name||'D')}&background=3b5bdb&color=fff&size=64`;
        userName.textContent = data.name || 'Demo User';
        enableSearch();

        // Check if we just came from Google Login
        if (sessionStorage.getItem('playLaunchAnim') === 'true') {
            sessionStorage.removeItem('playLaunchAnim');

            const overlay = $('launch-overlay');
            if (overlay) {
                // Make sure it's visible (CSS var already set it, but just in case)
                overlay.style.display = 'flex';

                // Restart animation by forcing a reflow
                const plane = $('plane-wrapper');
                if (plane) {
                    plane.style.animation = 'none';
                    void plane.offsetWidth;
                    plane.style.animation = '';
                }

                // After animation completes, hide overlay and show page
                setTimeout(() => {
                    overlay.style.display = 'none';
                    document.body.style.opacity = '1';
                }, 2100);
            }
        } else {
            // No animation — just reveal the page
            document.body.style.opacity = '1';
            const name = (data.name || 'there').split(' ')[0];
            setTimeout(() => showToast(`Welcome, ${name}! Start searching for buyers below.`, '👋'), 600);
        }
    })
    .catch(() => {
        window.location.replace('/login.html');
    });

function enableSearch() {
    searchInput.disabled = false;
    searchBtn.disabled   = false;
    if (loginWarning) loginWarning.classList.add('hidden');
    quickTags.forEach(t => { t.disabled = false; });
}

// ─── Search ───────────────────────────────────────────────────────────────
searchBtn.addEventListener('click', () => doSearch(searchInput.value.trim()));
searchInput.addEventListener('keydown', e => { if (e.key === 'Enter') doSearch(searchInput.value.trim()); });

quickTags.forEach(tag => {
    tag.addEventListener('click', () => {
        if (!isAuthenticated) return;
        const q = tag.dataset.query;
        searchInput.value = q;
        doSearch(q);
    });
});

newSearchBtn.addEventListener('click', () => {
    resultsSection.classList.add('hidden');
    resultsMeta.classList.add('hidden');
    searchInput.value = '';
    searchInput.focus();
    window.scrollTo({ top: 0, behavior: 'smooth' });
});

function doSearch(query) {
    if (!isAuthenticated || !query) {
        if (!query) {
            searchInput.style.borderColor = '#e8590c';
            setTimeout(() => searchInput.style.borderColor = '', 1500);
        }
        return;
    }

    const startTime = Date.now();

    // Show results section + scroll
    resultsSection.classList.remove('hidden');
    resultsMeta.classList.add('hidden');
    window.scrollTo({ top: resultsSection.offsetTop - 80, behavior: 'smooth' });

    // Loading state
    resultsTitle.textContent = `Results for "${query}"`;
    resultsCount.textContent = '';
    loadingState.classList.remove('hidden');
    buyersGrid.innerHTML = '';

    // Cycle loading messages
    let msgIdx = 0;
    updateLoadingMsg(msgIdx);
    clearInterval(loadingMsgTimer);
    loadingMsgTimer = setInterval(() => {
        msgIdx = (msgIdx + 1) % LOADING_MSGS.length;
        updateLoadingMsg(msgIdx);
    }, 2000);

    fetch(`/api/search?query=${encodeURIComponent(query)}`)
        .then(r => {
            if (!r.ok) throw new Error(`HTTP ${r.status}`);
            return r.json();
        })
        .then(data => {
            clearInterval(loadingMsgTimer);
            loadingState.classList.add('hidden');

            const elapsed = ((Date.now() - startTime) / 1000).toFixed(1);

            if (!data || data.length === 0) {
                buyersGrid.innerHTML = `
                    <div class="empty-state">
                        <div class="empty-state-icon"><i class="fa-regular fa-face-meh"></i></div>
                        <div class="empty-state-title">No buyers found</div>
                        <div class="empty-state-msg">Try a different search term or US location</div>
                    </div>`;
                return;
            }

            resultsCount.textContent = `${data.length} found`;
            renderCards(data);

            // Show meta
            resultsMeta.classList.remove('hidden');
            resultsTime.textContent = `Search took ${elapsed}s`;

            showToast(`Found ${data.length} buyers for "${query}"`, '🎯', 4000);
        })
        .catch(err => {
            clearInterval(loadingMsgTimer);
            loadingState.classList.add('hidden');
            buyersGrid.innerHTML = `
                <div class="error-state">
                    <div class="error-state-icon"><i class="fa-solid fa-triangle-exclamation"></i></div>
                    <div class="error-state-title">Something went wrong</div>
                    <div class="error-state-msg">Please check your connection and try again.</div>
                </div>`;
            console.error('Search error:', err);
        });
}

function updateLoadingMsg(idx) {
    const el1 = $('loading-headline');
    const el2 = $('loading-sub');
    if (!el1 || !el2) return;
    el1.style.opacity = '0';
    el2.style.opacity = '0';
    setTimeout(() => {
        el1.textContent = LOADING_MSGS[idx][0];
        el2.textContent = LOADING_MSGS[idx][1];
        el1.style.transition = 'opacity 0.4s ease';
        el2.style.transition = 'opacity 0.4s ease';
        el1.style.opacity = '1';
        el2.style.opacity = '1';
    }, 200);
}

// ─── Render Cards ─────────────────────────────────────────────────────────
function renderCards(buyers) {
    buyers.forEach((buyer, i) => {
        const grad    = AVATAR_COLORS[i % AVATAR_COLORS.length];
        const initial = (buyer.name || 'B').charAt(0).toUpperCase();
        const email   = buyer.email || '';
        const hasEmail = email && email !== 'N/A' && email !== 'No email' && email.includes('@');
        const website = buyer.website || '';
        const websiteUrl = website ? (website.startsWith('http') ? website : 'https://' + website) : '#';

        const card = document.createElement('div');
        card.className = 'buyer-card';
        card.style.animationDelay = `${i * 0.07}s`;
        card.innerHTML = `
            <div class="card-header">
                <div class="card-avatar" style="background:${grad}">${initial}</div>
                <div class="card-titles">
                    <div class="card-name" title="${esc(buyer.name || 'Unknown')}">${esc(buyer.name || 'Unknown')}</div>
                    <div class="card-company" title="${esc(buyer.company || '')}">${esc(buyer.company || 'Independent')}</div>
                </div>
            </div>

            <div class="card-body">
                <div class="card-detail">
                    <div class="card-detail-icon"><i class="fa-solid fa-envelope"></i></div>
                    <span class="card-detail-text">${esc(email || 'Not available')}</span>
                </div>
                <div class="card-detail">
                    <div class="card-detail-icon"><i class="fa-solid fa-location-dot"></i></div>
                    <span class="card-detail-text">${esc(buyer.location || 'Location unknown')}</span>
                </div>
                <div class="card-detail">
                    <div class="card-detail-icon"><i class="fa-solid fa-globe"></i></div>
                    <span class="card-detail-text">
                        ${website
                            ? `<a href="${esc(websiteUrl)}" target="_blank" rel="noopener" style="color:var(--brand);text-decoration:none;">${esc(website)}</a>`
                            : 'No website'}
                    </span>
                </div>
            </div>

            <div class="card-footer">
                <button class="btn-pitch pitch-btn" ${hasEmail ? '' : 'disabled'} data-email="${esc(email)}" data-name="${esc(buyer.name || '')}" data-company="${esc(buyer.company || '')}">
                    <i class="fa-solid fa-paper-plane"></i>
                    ${hasEmail ? 'Send Pitch' : 'No Email'}
                </button>
            </div>
        `;
        buyersGrid.appendChild(card);
    });

    document.querySelectorAll('.pitch-btn').forEach(btn => {
        if (!btn.disabled) {
            btn.addEventListener('click', (e) => {
                const icon = btn.querySelector('i');
                if (icon) {
                    // Prevent multiple clicks
                    btn.disabled = true;
                    // Start flight animation
                    icon.classList.add('fly-active');
                    
                    setTimeout(() => {
                        icon.classList.remove('fly-active');
                        btn.disabled = false;
                        openModal(btn.dataset.email, btn.dataset.name, btn.dataset.company);
                    }, 1300); // Wait for flight to finish
                } else {
                    openModal(btn.dataset.email, btn.dataset.name, btn.dataset.company);
                }
            });
        }
    });
}

// Escape HTML
function esc(str) {
    const el = document.createElement('span');
    el.textContent = str;
    return el.innerHTML;
}

// ─── Modal ────────────────────────────────────────────────────────────────
function openModal(email, name, company) {
    emailTo.value = email;
    modalBuyerName.textContent = `${name} · ${company}`;
    emailSubject.value = `Partnership Opportunity — ${company}`;
    emailStatus.textContent = '';
    emailStatus.className = 'email-status';
    sendEmailBtn.disabled = false;
    sendEmailBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i><span>Send Email</span>';
    emailModal.classList.remove('hidden');
    document.body.style.overflow = 'hidden';
}

function closeModalFn() {
    emailModal.classList.add('hidden');
    document.body.style.overflow = '';
}

closeModal.addEventListener('click', closeModalFn);
emailModal.addEventListener('click', e => { if (e.target === emailModal) closeModalFn(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModalFn(); });

// ─── Send Email ───────────────────────────────────────────────────────────
sendEmailBtn.addEventListener('click', () => {
    const payload = {
        to:      emailTo.value.trim(),
        subject: emailSubject.value.trim(),
        body:    emailBody.value.trim()
    };

    if (!payload.to || !payload.subject || !payload.body) {
        showStatus('Please fill all fields.', false);
        return;
    }

    sendEmailBtn.disabled  = true;
    sendEmailBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i><span>Sending…</span>';

    fetch('/api/email', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
    })
    .then(r => r.json())
    .then(data => {
        if (data.error) throw new Error(data.error);
        showStatus('✓ Email sent successfully!', true);
        sendEmailBtn.innerHTML = '<i class="fa-solid fa-check"></i><span>Sent!</span>';
        showToast('Pitch email sent! 🎉', '📬', 4000);
        setTimeout(closeModalFn, 2000);
    })
    .catch(() => {
        showStatus('SMTP not configured. Set MAIL_USERNAME and MAIL_PASSWORD.', false);
        sendEmailBtn.disabled  = false;
        sendEmailBtn.innerHTML = '<i class="fa-solid fa-paper-plane"></i><span>Retry</span>';
    });
});

function showStatus(msg, ok) {
    emailStatus.textContent = msg;
    emailStatus.className   = `email-status ${ok ? 'status-ok' : 'status-err'}`;
}

// ─── Navbar scroll ────────────────────────────────────────────────────────
window.addEventListener('scroll', () => {
    $('navbar').classList.toggle('scrolled', window.scrollY > 10);
}, { passive: true });
