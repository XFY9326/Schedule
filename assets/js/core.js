const LATEST_APK_URL = "https://update.xfy9326.top/Schedule/LatestDownload";

function showToast(text, color) {
    Toastify({
        text: text,
        duration: 1500,
        gravity: "top",
        position: "center",
        style: {
            "font-size": "0.8em",
            background: color
        },
        offset: {
            y: "2em"
        }
    }).showToast();
}

function onLoadClipBoard() {
    let clipBoard = new ClipboardJS('.copy-element');
    clipBoard.on('success', function (e) {
        showToast("复制成功", "#00bbf0");
        e.clearSelection();
    });

    clipBoard.on('error', function () {
        showToast("复制失败", "#fdb44b");
    });
}

function onImportJSAdapter(config) {
    window.location.href = "pusc://course_import/js_config?src=" + config;
    showToast("尝试跳转中", "#3cb371");
}

async function getLatestAPKVersionInfo() {
    let content = await fetch(LATEST_APK_URL).then((r) => r.json());
    return content.versionName + " (" + content.versionCode + ")";
}

async function downloadLatestAPK() {
    let content = await fetch(LATEST_APK_URL).then((r) => r.json());
    window.open(content.url);
}

(async function () {
    const info = document.getElementById("latestVersionInfo");
    if (info) info.innerText = await getLatestAPKVersionInfo();
})();
