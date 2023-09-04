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
    showToast("跳转中", "#3cb371");
}
