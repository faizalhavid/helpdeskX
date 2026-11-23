import axios from 'axios';



export const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_URL || '',
    timeout : 10000,
    headers: {'Content-Type' : 'application/json'},
});

api.interceptors.request.use((config) => {
  if (typeof window !== 'undefined') {
      const token = localStorage.getItem('token');
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
},
(error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response)=>response,
    (error) => {
        if(error.response?.status === 401 && typeof window !== 'undefined'){
            localStorage.removeItem('token');
            window.location.href = '/login'
        }
        
        const customMessage = error.response?.data?.message || error.message || 'Something went error';
        console.log('API Error:', error);
        return Promise.reject(new Error(customMessage));
}
)