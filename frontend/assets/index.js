import css from './style.css?inline';

document.head.appendChild(Object.assign(document.createElement('style'), { textContent: css }));
