/**
 * EZWeb — Main JavaScript
 * Rolling door, cart, chatbot, misc UI
 */
(function () {
  'use strict';

  // ─────────────────────────────────────────────────────────────
  // ROLLING DOOR
  // ─────────────────────────────────────────────────────────────
  var DOOR_KEY = 'ezweb_door_open';
  var door = document.getElementById('ez-door');

  function isDoorOpen() {
    try {
      return localStorage.getItem(DOOR_KEY) === 'true';
    } catch (e) {
      return false;
    }
  }

  function setDoorState(open) {
    try {
      localStorage.setItem(DOOR_KEY, open ? 'true' : 'false');
    } catch (e) {}
    if (door) {
      if (open) {
        door.classList.add('is-open');
        door.setAttribute('aria-hidden', 'true');
      } else {
        door.classList.remove('is-open');
        door.setAttribute('aria-hidden', 'false');
      }
    }
    document.documentElement.setAttribute('data-door', open ? 'open' : 'closed');
  }

  function openDoor() {
    setDoorState(true);
  }
  function closeDoor() {
    setDoorState(false);
  }

  // Init on load
  if (isDoorOpen()) {
    openDoor();
  }

  // "Go to store" buttons
  ['btn-open-store', 'btn-open-store-hero', 'btn-open-store-hiw'].forEach(function (id) {
    var btn = document.getElementById(id);
    if (btn) btn.addEventListener('click', openDoor);
  });

  // "Show landing" button in header
  var showLanding = document.getElementById('btn-show-landing');
  if (showLanding) {
    showLanding.addEventListener('click', function () {
      closeDoor();
      setTimeout(function () {
        if (door) door.scrollTo({ top: 0, behavior: 'smooth' });
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }, 100);
    });
  }

  // ─────────────────────────────────────────────────────────────
  // DARK MODE TOGGLE
  // ─────────────────────────────────────────────────────────────
  var darkModeToggle = document.getElementById('darkModeToggle');
  if (darkModeToggle) {
    darkModeToggle.addEventListener('click', function () {
      var isDark = document.documentElement.classList.toggle('dark');
      try {
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
      } catch (e) {}
    });
  }

  // ─────────────────────────────────────────────────────────────
  // USER MENU DROPDOWN
  // ─────────────────────────────────────────────────────────────
  var userMenuTrigger = document.getElementById('userMenuTrigger');
  var userMenuDropdown = document.getElementById('userMenuDropdown');
  if (userMenuTrigger && userMenuDropdown) {
    userMenuTrigger.addEventListener('click', function (e) {
      e.stopPropagation();
      userMenuDropdown.classList.toggle('hidden');
    });
    // Close on outside click
    document.addEventListener('click', function (e) {
      if (!userMenuDropdown.classList.contains('hidden')) {
        if (!userMenuTrigger.contains(e.target) && !userMenuDropdown.contains(e.target)) {
          userMenuDropdown.classList.add('hidden');
        }
      }
    });
  }

  // ─────────────────────────────────────────────────────────────
  // CSRF TOKEN helper (for AJAX)
  // ─────────────────────────────────────────────────────────────
  function getCsrfToken() {
    var meta = document.querySelector('meta[name="_csrf"]');
    return meta ? meta.getAttribute('content') : '';
  }
  function getCsrfHeader() {
    var meta = document.querySelector('meta[name="_csrf_header"]');
    return meta ? meta.getAttribute('content') : 'X-CSRF-TOKEN';
  }

  // ─────────────────────────────────────────────────────────────
  // CART — add to cart via AJAX (used in product-detail)
  // Exposed globally so inline onclick can call it
  // ─────────────────────────────────────────────────────────────
  window.addToCart = function (btn) {
    var productId = btn.getAttribute('data-product-id');
    btn.disabled = true;
    btn.innerHTML = '<i class="ph ph-hourglass"></i> Đang thêm...';

    var headers = { 'Content-Type': 'application/json' };
    var csrfHeader = getCsrfHeader();
    var csrfToken = getCsrfToken();
    if (csrfToken) headers[csrfHeader] = csrfToken;

    fetch('/cart/add/' + productId, { method: 'POST', headers: headers })
      .then(function (r) {
        return r.json();
      })
      .then(function (data) {
        if (data.success) {
          btn.innerHTML = '<i class="ph ph-check-circle"></i> Đã thêm vào giỏ';
          btn.classList.remove('ez-btn-primary');
          btn.classList.add('ez-btn-secondary');
          updateCartBadge(data.cartCount);
        } else {
          btn.disabled = false;
          btn.innerHTML = '<i class="ph ph-shopping-cart-simple"></i> Thêm vào giỏ';
        }
      })
      .catch(function () {
        btn.disabled = false;
        btn.innerHTML = '<i class="ph ph-shopping-cart-simple"></i> Thêm vào giỏ';
      });
  };

  function updateCartBadge(count) {
    var badge = document.getElementById('cart-count-badge');
    if (count > 0) {
      if (badge) {
        badge.textContent = count;
        badge.classList.remove('hidden');
      } else {
        var cartBtn = document.getElementById('cart-btn');
        if (cartBtn) {
          var b = document.createElement('span');
          b.id = 'cart-count-badge';
          b.className =
            'absolute -top-1 -right-1 min-w-[18px] h-[18px] flex items-center justify-center bg-red-500 text-white text-[10px] font-bold rounded-full px-1';
          b.textContent = count;
          cartBtn.appendChild(b);
        }
      }
    } else if (badge) {
      badge.remove();
    }
  }

  // ─────────────────────────────────────────────────────────────
  // AI CHATBOT
  // ─────────────────────────────────────────────────────────────
  var chatToggle = document.getElementById('ez-chat-toggle');
  var chatWindow = document.getElementById('ez-chat-window');
  var chatClose = document.getElementById('ez-chat-close');
  var chatInput = document.getElementById('ez-chat-input');
  var chatSend = document.getElementById('ez-chat-send');
  var chatMessages = document.getElementById('ez-chat-messages');

  if (chatToggle && chatWindow) {
    chatToggle.addEventListener('click', function () {
      var isHidden = chatWindow.classList.contains('hidden');
      if (isHidden) {
        chatWindow.classList.remove('hidden');
        chatWindow.style.display = 'flex';
        chatWindow.style.flexDirection = 'column';
        if (chatInput) chatInput.focus();
      } else {
        chatWindow.classList.add('hidden');
        chatWindow.style.display = '';
      }
    });
  }

  if (chatClose) {
    chatClose.addEventListener('click', function () {
      if (chatWindow) {
        chatWindow.classList.add('hidden');
        chatWindow.style.display = '';
      }
    });
  }

  function appendChatMessage(text, role) {
    if (!chatMessages) return;
    var div = document.createElement('div');
    div.className = 'ez-chat-msg ' + role;
    if (role === 'bot' && typeof marked !== 'undefined') {
      div.innerHTML = marked.parse(text);
    } else {
      div.textContent = text;
    }
    chatMessages.appendChild(div);
    chatMessages.scrollTop = chatMessages.scrollHeight;
  }

  function sendChatMessage() {
    if (!chatInput || !chatMessages) return;
    var message = chatInput.value.trim();
    if (!message) return;

    appendChatMessage(message, 'user');
    chatInput.value = '';
    chatInput.disabled = true;
    if (chatSend) chatSend.disabled = true;

    // Loading indicator
    var loadingDiv = document.createElement('div');
    loadingDiv.className = 'ez-chat-msg bot';
    loadingDiv.innerHTML = '<i class="ph ph-hourglass ph-spin"></i> Đang trả lời...';
    chatMessages.appendChild(loadingDiv);
    chatMessages.scrollTop = chatMessages.scrollHeight;

    var headers = { 'Content-Type': 'application/json' };
    var csrfHeader = getCsrfHeader();
    var csrfToken = getCsrfToken();
    if (csrfToken) headers[csrfHeader] = csrfToken;

    fetch('/chat', {
      method: 'POST',
      headers: headers,
      body: JSON.stringify({ message: message }),
    })
      .then(function (r) {
        return r.json();
      })
      .then(function (data) {
        loadingDiv.remove();
        appendChatMessage(data.reply || 'Xin lỗi, tôi không thể trả lời lúc này.', 'bot');
      })
      .catch(function () {
        loadingDiv.remove();
        appendChatMessage('Có lỗi xảy ra. Vui lòng thử lại.', 'bot');
      })
      .finally(function () {
        if (chatInput) chatInput.disabled = false;
        if (chatSend) chatSend.disabled = false;
        if (chatInput) chatInput.focus();
      });
  }

  if (chatSend) {
    chatSend.addEventListener('click', sendChatMessage);
  }
  if (chatInput) {
    chatInput.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendChatMessage();
      }
    });
  }

  // ─────────────────────────────────────────────────────────────
  // COPY TO CLIPBOARD (topup page)
  // ─────────────────────────────────────────────────────────────
  window.copyText = function (btn) {
    var text = btn.getAttribute('data-copy');
    if (!navigator.clipboard) {
      // Fallback
      var ta = document.createElement('textarea');
      ta.value = text;
      document.body.appendChild(ta);
      ta.select();
      document.execCommand('copy');
      ta.remove();
      btn.innerHTML = '<i class="ph ph-check"></i>';
      setTimeout(function () {
        btn.innerHTML = '<i class="ph ph-copy"></i>';
      }, 2000);
      return;
    }
    navigator.clipboard.writeText(text).then(function () {
      btn.innerHTML = '<i class="ph ph-check"></i>';
      setTimeout(function () {
        btn.innerHTML = '<i class="ph ph-copy"></i>';
      }, 2000);
    });
  };

  // ─────────────────────────────────────────────────────────────
  // PASSWORD TOGGLE
  // ─────────────────────────────────────────────────────────────
  window.togglePassword = function (id, btn) {
    var input = document.getElementById(id);
    if (!input) return;
    if (input.type === 'password') {
      input.type = 'text';
      btn.innerHTML = '<i class="ph ph-eye-slash"></i>';
    } else {
      input.type = 'password';
      btn.innerHTML = '<i class="ph ph-eye"></i>';
    }
  };

  // ─────────────────────────────────────────────────────────────
  // SCROLL REVEAL (landing page sections)
  // ─────────────────────────────────────────────────────────────
  if (typeof IntersectionObserver !== 'undefined') {
    var revealObserver = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible');
            revealObserver.unobserve(entry.target);
          }
        });
      },
      { threshold: 0.12, rootMargin: '0px 0px -40px 0px' },
    );
    document.querySelectorAll('.ez-reveal').forEach(function (el) {
      revealObserver.observe(el);
    });
  }

  // ─────────────────────────────────────────────────────────────
  // COUNTER ANIMATION (data-count elements in STATS section)
  // ─────────────────────────────────────────────────────────────
  if (typeof IntersectionObserver !== 'undefined') {
    var counterObserver = new IntersectionObserver(
      function (entries) {
        entries.forEach(function (entry) {
          if (entry.isIntersecting && !entry.target.dataset.counted) {
            entry.target.dataset.counted = '1';
            var el = entry.target;
            var target = parseInt(el.getAttribute('data-count'), 10);
            var suffix = el.getAttribute('data-count-suffix') || '';
            var duration = 1800;
            var startTime = null;
            function step(ts) {
              if (!startTime) startTime = ts;
              var p = Math.min((ts - startTime) / duration, 1);
              var eased = p < 0.5 ? 2 * p * p : -1 + (4 - 2 * p) * p;
              el.textContent = Math.floor(eased * target) + suffix;
              if (p < 1) requestAnimationFrame(step);
              else el.textContent = target + suffix;
            }
            requestAnimationFrame(step);
          }
        });
      },
      { threshold: 0.5 },
    );
    document.querySelectorAll('[data-count]').forEach(function (el) {
      counterObserver.observe(el);
    });
  }

  // ─────────────────────────────────────────────────────────────
  // FAQ ACCORDION
  // ─────────────────────────────────────────────────────────────
  document.querySelectorAll('.faq-item').forEach(function (item) {
    var trigger = item.querySelector('.faq-trigger');
    if (trigger) {
      trigger.addEventListener('click', function () {
        var isOpen = item.classList.contains('open');
        document.querySelectorAll('.faq-item.open').forEach(function (i) {
          i.classList.remove('open');
        });
        if (!isOpen) item.classList.add('open');
      });
    }
  });

  // ─────────────────────────────────────────────────────────────
  // CHAT PRESET CHIPS
  // ─────────────────────────────────────────────────────────────
  var presetsEl = document.getElementById('ez-chat-presets');
  if (presetsEl) {
    // convert vertical wheel to horizontal scroll
    presetsEl.addEventListener(
      'wheel',
      function (e) {
        if (e.deltaY !== 0) {
          e.preventDefault();
          presetsEl.scrollLeft += e.deltaY * 1.2;
        }
      },
      { passive: false },
    );
  }

  document.querySelectorAll('.ez-chat-preset').forEach(function (btn) {
    btn.addEventListener('click', function () {
      var q = btn.getAttribute('data-q');
      if (chatInput && q) {
        chatInput.value = q;
        if (presetsEl) presetsEl.style.display = 'none';
        sendChatMessage();
      }
    });
  });
})();
