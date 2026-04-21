function showMessage(id, message, isError = false) {
  const block = document.getElementById(id);
  if (!block) return;
  block.textContent = message;
  block.className = isError ? 'feedback text-danger' : 'feedback text-success';
}

function logout() {
  localStorage.removeItem('token');
  localStorage.removeItem('role');
  window.location.href = '/frontend/pages/login.html';
}
