import type { MenuProps } from 'antd';
import { Menu } from 'antd';
import React from 'react';
import routesConfig from '../../src/components/Entity/routesConfig';
import { history } from '@umijs/max';

type MenuItem = Required<MenuProps>['items'][number];

function getItem(
  label: React.ReactNode,
  key: React.Key,
  icon?: React.ReactNode,
  children?: MenuItem[],
  type?: 'group',
): MenuItem {
  return {
    key,
    icon,
    children,
    label,
    type,
  } as MenuItem;
}

// 定义路由配置变量的类型
interface RouteConfig {
  path: string;
  layout?: boolean;
  routes?: RouteConfig[];
  name: string;
  component: string;
  redirect?: string;
}

// 递归构建菜单项的函数
function buildMenuItems(routes: RouteConfig[]): MenuItem[] {
  return routes
    .filter(
      (route) => route.redirect == null && !route.path.includes('*') && !route.path.includes(':'),
    ) // 过滤条件
    .map((route) => {
      const item: MenuItem = getItem(route.name, route.path);
      if (route.routes) {
        item.children = buildMenuItems(route.routes);
      }
      return item;
    });
}

// 调用递归函数构建菜单项
const menuItems: MenuProps['items'] = buildMenuItems(routesConfig);
/*
const items: MenuProps['items'] = [
  getItem('Navigation One', 'sub1', <MailOutlined />, [
    getItem('Item 1', 'g1', null, [getItem('Option 1', '1'), getItem('Option 2', '2')], 'group'),
    getItem('Item 2', 'g2', null, [getItem('Option 3', '3'), getItem('Option 4', '4')], 'group'),
  ]),

  getItem('Navigation Two', 'sub2', <AppstoreOutlined />, [
    getItem('Option 5', '5'),
    getItem('Option 6', '6'),
    getItem('Submenu', 'sub3', null, [getItem('Option 7', '7'), getItem('Option 8', '8')]),
  ]),

  { type: 'divider' },

  getItem('Navigation Three', 'sub4', <SettingOutlined />, [
    getItem('Option 9', '9'),
    getItem('Option 10', '10'),
    getItem('Option 11', '11'),
    getItem('Option 12', '12'),
  ]),

  getItem('Group', 'grp', null, [getItem('Option 13', '13'), getItem('Option 14', '14')], 'group'),
];*/

const MyMenu: React.FC = () => {
  const onClick: MenuProps['onClick'] = (e) => {
    // console.log('click ', e);
    history.push(e.key);
  };

  return (
    <div
      style={{
        position: 'absolute',
        borderRadius: '8px',
        fontSize: '14px',
        lineHeight: '22px',
        padding: '16px 19px',
        minWidth: '220px',
        flex: 1,
      }}
    >
      <Menu
        onClick={onClick}
        style={{ width: 256, position: 'absolute' }}
        defaultSelectedKeys={[]}
        defaultOpenKeys={[]}
        mode="inline"
        items={menuItems}
      />
    </div>
  );
};

export default MyMenu;
