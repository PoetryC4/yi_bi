export default [
  {
    path: '/user',
    layout: false,
    routes: [
      { path: '/user/login', name: 'userLogin', component: './User/Login' },
      {
        path: '/user/register',
        name: "userRegister",
        component: './User/Register',
      },
    ],
  },
  { path: '/welcome', icon: 'smile', component: './Welcome' },
  {
    path: '/admin',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      { path: '/admin', name: 'admin1', redirect: '/admin/sub-page' },
      { path: '/admin/sub-page', name: 'admin2', component: './Admin' },
    ],
  },
  { icon: 'table', path: '/list', component: './TableList' },
  { path: '/', redirect: '/welcome' },
  { path: '*', layout: false, component: './404' },
];
