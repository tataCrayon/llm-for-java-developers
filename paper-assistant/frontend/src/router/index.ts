import {createRouter, createWebHistory} from 'vue-router'
import {useAuthStore} from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/dashboard'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: {requiresAuth: false}
    },
    {
      path: '/dashboard',
      name: 'dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: {requiresAuth: true}
    },
    {
      path: '/chat',
      name: 'chat',
      component: () => import('@/views/ChatView.vue'),
      meta: {requiresAuth: true}
    },
    {
      path: '/papers',
      name: 'papers',
      component: () => import('@/views/PapersView.vue'),
      meta: {requiresAuth: true},
      children: [
        {
          path: 'search',
          name: 'paper-search',
          component: () => import('@/views/papers/PaperSearchView.vue')
        },
        {
          path: 'analyze',
          name: 'paper-analyze',
          component: () => import('@/views/papers/PaperAnalyzeView.vue')
        },
        {
          path: 'compare',
          name: 'paper-compare',
          component: () => import('@/views/papers/PaperCompareView.vue')
        }
      ]
    },
    {
      path: '/learning',
      name: 'learning',
      component: () => import('@/views/LearningView.vue'),
      meta: {requiresAuth: true}
    },
    {
      path: '/resources',
      name: 'resources',
      component: () => import('@/views/ResourcesView.vue'),
      meta: {requiresAuth: true}
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
