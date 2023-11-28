import { Image, Layout, Typography } from 'antd';

const { Header, Content } = Layout;
const { Title, Text } = Typography;

const WebsiteTitle = () => {
  return (
    <Header>
      <div
        className="logo"
        style={{ display: 'flex', flexDirection: 'row', width: 200, inset: '0', margin: 'auto' }}
      >
        {/*<HomeOutlined style={{ fontSize: '24px', color: '#fff' }} />*/}
        <Image width={100} src="/logo.svg" preview={false} />
        <Title level={1} style={{ color: '#222', marginLeft: '12px', inset: '0', margin: 'auto' }}>
          翼 BI
        </Title>
      </div>
      <div
        style={{ display: 'flex', flexDirection: 'row', width: 200, inset: '0', margin: 'auto' }}
      >
        <Title level={2} style={{ color: '#222', marginLeft: '12px', inset: '0', margin: 'auto' }}>
          添加图表
        </Title>
      </div>
    </Header>
  );
};

export default WebsiteTitle;
