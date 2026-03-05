(function () {
  function normalize(path) {
    return (path || "").replace(/\/+$/, "");
  }

  document.addEventListener("DOMContentLoaded", function () {
    var current = normalize(window.location.pathname);

    // Best-effort active state for navbar links
    document.querySelectorAll(".navbar a.nav-link").forEach(function (a) {
      try {
        var href = a.getAttribute("href") || "";
        if (!href || href.startsWith("#")) return;
        var url = new URL(href, window.location.origin);
        var target = normalize(url.pathname);
        if (target && (current === target || (target !== "" && current.startsWith(target) && target !== "/"))) {
          a.classList.add("active");
        }
      } catch (_) {
        // ignore
      }
    });
  });
})();
