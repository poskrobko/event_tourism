function resolveApiBaseUrl() {
  const savedApiUrl = localStorage.getItem('apiUrl');
  if (savedApiUrl) return savedApiUrl;

  const { protocol, hostname } = window.location;
  const apiHost = hostname || 'localhost';
  return `${protocol}//${apiHost}:8080/api`;
}

const API_URL = resolveApiBaseUrl();

function getToken() {
  return localStorage.getItem('token');
}

async function request(path, options = {}) {
  const headers = options.headers || {};
  const token = getToken();
  if (token) headers['Authorization'] = `Bearer ${token}`;
  headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(`${API_URL}${path}`, { ...options, headers });
  } catch (error) {
    throw new Error('Не удалось подключиться к серверу API. Проверьте, что backend запущен на порту 8080.');
  }

  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({ message: 'Unknown error' }));
    throw new Error(errorBody.message || 'Request failed');
  }
  return response.status === 204 ? null : response.json();
}
