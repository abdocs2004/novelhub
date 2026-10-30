import { api } from './client.js'

const data = (promise) => promise.then((response) => response.data)

export const authApi = {
  login: (body) => data(api.post('/auth/login', body)),
  register: (body) => data(api.post('/auth/register', body)),
  me: () => data(api.get('/auth/me')),
}

export const novelApi = {
  list: () => data(api.get('/novels')),
  get: (id) => data(api.get(`/novels/${id}`)),
}

export const chapterApi = {
  read: (id) => data(api.get(`/chapters/${id}`)),
}

export const purchaseApi = {
  buy: (chapterId) => data(api.post(`/purchases/${chapterId}`)),
  mine: () => data(api.get('/purchases/my')),
}

export const adminApi = {
  createNovel: (body) => data(api.post('/admin/novels', body)),
  updateNovel: (id, body) => data(api.put(`/admin/novels/${id}`, body)),
  deleteNovel: (id) => data(api.delete(`/admin/novels/${id}`)),
  chapters: (novelId) => data(api.get(`/admin/novels/${novelId}/chapters`)),
  createChapter: (novelId, body) => data(api.post(`/admin/novels/${novelId}/chapters`, body)),
  updateChapter: (id, body) => data(api.put(`/admin/chapters/${id}`, body)),
  deleteChapter: (id) => data(api.delete(`/admin/chapters/${id}`)),
  users: () => data(api.get('/admin/users')),
  purchases: () => data(api.get('/admin/purchases')),
}
