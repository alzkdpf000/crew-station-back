const memberService = (() => {
    const login = async (member) => {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            body: JSON.stringify(member),
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Fetch error");
        }

        return await response.json();
    }

    const refresh = async () => {
        const response = await fetch('/api/auth/refresh', {
            method: 'POST',
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Fetch error");
        }

        return await response.json();
    }

    const logout = async () => {
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Fetch error");
        }
    }

    const info = async (callback) => {
        const response = await fetch('/api/auth/info');
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Fetch error");
        }

        const member = await response.json();

        if(callback) {
            callback(member);
        }
    }



    return {login: login, refresh: refresh, logout: logout, info: info};
})();


const loginSection = document.querySelector('.login-section');
const userSection = document.querySelector('.user-section');
const profileImg = document.querySelector('.member-profile-wrap img');
const nameEl = document.querySelector('.member-name');
const alarmCount = document.querySelector('.alarm-count');

// 기본 상태: 비로그인
if (loginSection) loginSection.classList.remove('hidden');
if (userSection) userSection.classList.add('hidden');

// 로그인 정보 불러오기
memberService.info(async (member) => {
    if (!member) {
        // 비로그인 상태 유지
        if (loginSection) loginSection.classList.remove('hidden');
        if (userSection) userSection.classList.add('hidden');
        return;
    }

    // 로그인 상태
    if (loginSection) loginSection.classList.add('hidden');
    if (userSection) userSection.classList.remove('hidden');

    // 프로필 이미지 세팅
    if (profileImg) {
        profileImg.src =
            member.filePath && member.filePath.trim() !== ''
                ? member.filePath
                : '/images/default-profile.png';
    }

    // 이름 or 이메일 표시
    if (nameEl) {
        nameEl.textContent = member.memberName || member.memberEmail;
    }

    // 알림 개수 가져오기
    try {
        const res = await fetch('/api/alarms/count');
        if (res.ok) {
            const { count } = await res.json();
            if (alarmCount) alarmCount.textContent = count;
        }
    } catch (e) {
        console.error('알림 개수 요청 오류:', e);
    }
});