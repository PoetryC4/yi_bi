import { options } from '@/components/Entity/Enum/ChartTypeEnum';
import { addChartUsingPost } from '@/services/yibi-frontend/chartController';
import { useModel } from '@@/exports';
import { UploadOutlined } from '@ant-design/icons';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { Button, Form, Image, Input, message, Select, Upload } from 'antd';
import Title from 'antd/es/typography/Title';
import React, { useState } from 'react';
import { history } from '@umijs/max';

const layout = {
  labelCol: { span: 8 },
  wrapperCol: { span: 16 },
};
const normFile = (e: any) => {
  console.log('Upload event:', e);
  if (Array.isArray(e)) {
    return e;
  }
  return e.fileList.slice(-1);
};
const validateMessages = {
  required: '${label} is required!',
};
/* eslint-enable no-template-curly-in-string */

const ChartAdd: React.FC = () => {
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
  const { initialState, setInitialState } = useModel('@@initialState');
  const chartAddClass = useEmotionCss(() => {
    return {
      display: 'flex',
      flexDirection: 'column',
      overflow: 'auto',
      height: '100vh',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
      backgroundColor: '#fafafa',
    };
  });

  const [loading, setLoading] = useState(false);

  if (!initialState) {
    return loading;
  }

  const { currentUser } = initialState;

  if (!currentUser) {
    history.push('/user/login?redirect=/chart/add');
    return undefined;
  }

  const [form] = Form.useForm();

  const onFinish = async (values: any) => {
    let res = await addChartUsingPost(values, {}, values.file[0]);
    if (res.code === 0) {
      history.push(`/chart/result/${res.data}`);
    } else {
      message.error(res.message);
    }
  };
  return (
    <div className={chartAddClass}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          height: 50,
          inset: '0',
          margin: '20px auto',
        }}
      >
        {/*<HomeOutlined style={{ fontSize: '24px', color: '#fff' }} />*/}
        <Image width={50} src="/logo.svg" preview={false} style={{ marginRight: 12 }} />
        <Title level={2} style={{ color: '#222', inset: '0', margin: 'auto', marginLeft: 14 }}>
          翼 BI
        </Title>
      </div>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          flexWrap: 'wrap',
          height: 50,
          inset: '0',
          margin: '1px auto',
        }}
      >
        {/*<HomeOutlined style={{ fontSize: '24px', color: '#fff' }} />*/}
        <Title level={3} style={{ color: '#222', textAlign: 'center' }}>
          添加图表
        </Title>
      </div>
      <Form
        {...layout}
        form={form}
        name="nest-messages"
        onFinish={onFinish}
        style={{ width: 800, inset: 0, margin: '1% auto auto auto' }}
        validateMessages={validateMessages}
      >
        <Form.Item
          name="title"
          label="标题"
          rules={[{ required: true, message: '请输入你的标题' }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          name="goal"
          label="分析需求"
          rules={[{ required: true, message: '请输入你的分析需求' }]}
        >
          <Input.TextArea showCount maxLength={1024} style={{ height: 120, resize: 'none' }} />
        </Form.Item>
        <Form.Item name="chartType" label="图表类型">
          <Select placeholder="你期望的图表类型, 不选表示由AI决定" allowClear>
            {options.map((option) => (
              <Select.Option key={option.value} value={option.value}>
                {option.label}
              </Select.Option>
            ))}
          </Select>
        </Form.Item>
        <Form.Item
          name="file"
          label="数据文件"
          valuePropName="fileList"
          getValueFromEvent={normFile}
        >
          <Upload name="logo" action="/upload.do" listType="picture">
            <Button icon={<UploadOutlined />}>点击上传数据文件</Button>
          </Upload>
        </Form.Item>
        <Form.Item wrapperCol={{ ...layout.wrapperCol, offset: 8 }}>
          <Button type="primary" htmlType="submit" style={{ width: '100%' }}>
            Submit
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};
export default ChartAdd;
