// src/router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import { isLoggedIn } from '../utils/auth.js'

const SKIP_AUTH = import.meta.env.VITE_SKIP_AUTH === 'true'

const routes = [
  {
    path: '/',
    name: 'Intro',
    component: () => import('../views/Intro/Index.vue') // 介绍首屏
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Auth/Login.vue')
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/Auth/ForgotPassword.vue')
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('../views/Auth/ResetPassword.vue')
  },
  {
    path: '/setup',
    name: 'Onboarding',
    component: () => import('../views/Onboarding/Index.vue'), // 首次使用引导画像生成
    meta: { requiresAuth: true }
  },
  {
    path: '/app',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard/Index.vue') },
      { path: 'agent-chat', name: 'AgentChat', component: () => import('../views/AgentChat/Index.vue') },
      { path: 'agent-hub', name: 'AgentHub', component: () => import('../views/Agent/Hub.vue') },
      { path: 'resource', name: 'Resource', component: () => import('../views/Resource/Index.vue') },
      { path: 'topology', name: 'Topology', component: () => import('../views/Resource/Topology.vue') },
      { path: 'calendar', name: 'Calendar', component: () => import('../views/Dashboard/Calendar.vue') },
      { path: 'quiz', name: 'Quiz', component: () => import('../views/Quiz/Index.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/Profile/Index.vue') },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/Profile/User.vue') },
      { path: 'message', name: 'Message', component: () => import('../views/Profile/Message.vue') },
      { path: 'notification', name: 'Notification', component: () => import('../views/Profile/Notification.vue') },
      { path: 'community', name: 'Community', component: () => import('../views/Community/Index.vue') },
      // src/router/index.js (片段补充)
{ path: 'video', name: 'Video', component: () => import('../views/Resource/Video.vue') },
{ path: 'document', name: 'Document', component: () => import('../views/Resource/Document.vue') },
      { path: 'article/:id', name: 'Article', component: () => import('../views/Community/Article.vue') },
      { path: 'publish', name: 'Publish', component: () => import('../views/Community/Publish.vue') },
      { path: 'settings', name: 'Settings', component: () => import('../views/Settings/Index.vue') },
      { path: 'topology', name: 'Topology', component: () => import('../views/Resource/Topology.vue') },
      { path: 'skill-tree', name: 'SkillTree', component: () => import('../views/Dashboard/SkillTree.vue') },
      { path: 'explore', name: 'Explore', component: () => import('../views/Explore/Index.vue') }
    ]
  },
  { 
    path: '/:pathMatch(.*)*', 
    name: 'NotFound', 
    component: () => import('../views/NotFound.vue') 
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (SKIP_AUTH) return true
  if (to.meta.requiresAuth && !isLoggedIn()) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.path === '/login' && isLoggedIn()) {
    return '/app/dashboard'
  }
  return true
})

export default router