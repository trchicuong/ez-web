/**
 * EZWeb — Admin JavaScript
 */
(function () {
  'use strict';

  // Confirm delete
  document.querySelectorAll('[data-confirm]').forEach(function (el) {
    el.addEventListener('click', function (e) {
      if (!confirm(el.getAttribute('data-confirm'))) {
        e.preventDefault();
      }
    });
  });

  // Auto-generate slug from title
  var titleInput = document.querySelector('[name="title"]');
  var slugInput = document.querySelector('[name="slug"]');
  if (titleInput && slugInput) {
    titleInput.addEventListener('input', function () {
      if (!slugInput._dirty) {
        slugInput.value = slugify(titleInput.value);
      }
    });
    slugInput.addEventListener('input', function () {
      slugInput._dirty = slugInput.value.length > 0;
    });
  }

  function slugify(text) {
    return text
      .toLowerCase()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/[đĐ]/g, 'd')
      .replace(/[^a-z0-9\s-]/g, '')
      .trim()
      .replace(/[\s_]+/g, '-')
      .replace(/-+/g, '-');
  }
})();
