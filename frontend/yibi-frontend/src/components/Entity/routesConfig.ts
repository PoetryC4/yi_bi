const routesConfig = [
  {
    path: '/user',
    layout: false,
    name: '用户',
    routes: [
      { path: '/user/login', name: 'userLogin', component: './User/Login' },
      {
        path: '/user/register',
        name: 'userRegister',
        component: './User/Register',
      },
    ],
  },
  {
    path: '/chart',
    layout: false,
    name: '图表',
    routes: [
      { path: '/chart/add', name: 'chartAdd', component: './Chart/Add' },
      { path: '/chart/table', name: 'chartTable', component: './Chart/Table' },
      { path: '/chart/result/:id', name: 'chartResult', component: './Chart/Result' },
    ],
  },
  { path: '/welcome', icon: 'smile', component: './Welcome', name: '欢迎界面' },
  {
    name: '管理员',
    path: '/admin',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      { path: '/admin', name: 'admin1', redirect: '/admin/sub-page' },
      { path: '/admin/sub-page', name: 'admin2', component: './Admin' },
    ],
  },
  { icon: 'table', path: '/list', component: './TableList', name: 'table' },
  { path: '/', redirect: '/welcome' },
  { path: '*', layout: false, component: './404' },
];

export default routesConfig;
