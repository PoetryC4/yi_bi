import { getChartVoByIdUsingGet } from '@/services/yibi-frontend/chartController';
import { useModel } from '@@/exports';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { sleep } from '@antfu/utils';
import { history } from '@umijs/max';
import {Button, Card, Col, Divider, message, Row} from 'antd';
import Title from 'antd/es/typography/Title';
import Paragraph from 'antd/lib/typography/Paragraph';
import ReactECharts from 'echarts-for-react';
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router';


const ChartResult: React.FC = () => {
  const { initialState, setInitialState } = useModel('@@initialState');
  const chartResultClass = useEmotionCss(() => {
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

  const { id } = useParams();

  const [loading, setLoading] = useState(false);
  const [chartVals, setChartVals] = useState<API.ChartVO>();
  const [isFinished, setIsFinished] = useState<number | undefined>();
  const [echartsOption, setEchartsOption] = useState<any>();

  if (!initialState) {
    return loading;
  }

  const { currentUser } = initialState;

  if (!currentUser) {
    history.push(`/user/login?redirect=/chart/result/${id}`);
  }

  const handleCopyCase = () => {
    history.push(`/chart/add?copy=${id}`)
  }

  const tryGetChart = async () => {
    let res = await getChartVoByIdUsingGet({
      id: id,
    });
    if (res.code === 0) {
      if (res.data.isFinished === 1) {
        if (currentUser.id !== res.data.userId) {
          message.error('你不得查看他人的结果');
          history.back();
          return;
        }
        let indexOfThis = (res.data.genCode || '').indexOf('{');
        if (indexOfThis === -1) {
          message.error('图表解析出错，请重试');
        } else {
          const refinedJsonStr = res.data.genCode
            .substring(indexOfThis)
            .replace(/(['"])?([a-zA-Z0-9_]+)(['"])?:/g, '"$2": ')
            .replace(/'/g, '"');
          console.log(refinedJsonStr);
          const genCode = JSON.parse(refinedJsonStr);
          console.log(genCode);
          if (!genCode) {
            message.error('图表解析出错，请重试');
          } else {
            setChartVals(res.data);
            setEchartsOption(genCode);
            setIsFinished(0);
            setLoading(false);
          }
        }
        return;
      } else {
        await sleep(3000);
        if (!isFinished) {
          await tryGetChart();
        }
        return;
      }
    } else {
      message.error(res.message);
    }
  };

  useEffect(() => {
    tryGetChart();
  }, []); // 在组件挂载时执行一次

  return (
    <div className={chartResultClass}>
      <Row>
        <Col span={10} style={{ fontSize: 40, color: '#bfbfbf' }}>
          <Card
            title={chartVals?.title || 'title'}
            bordered={true}
            style={{
              width: '90%',
              inset: 0,
              margin: '50px auto auto auto',
            }}
            loading={!chartVals}
            extra={<Button type="primary" onClick={handleCopyCase}>重新进行分析</Button>}
          >
            <Title level={3}>分析需求</Title>
            <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{chartVals?.goal || 'goal'}</Paragraph>
            <Divider style={{ margin: '8% auto 4% auto' }} />
            <Title level={3}>原始数据</Title>
            <Paragraph style={{ whiteSpace: 'pre-wrap' }}>
              {chartVals?.chartData || 'chartData'}
            </Paragraph>
          </Card>
        </Col>
        <Col span={14} style={{ fontSize: 40, color: '#bfbfbf' }}>
          <Card
            title="分析结果"
            bordered={true}
            style={{ width: '90%', inset: 0, margin: '50px auto auto auto' }}
            loading={!chartVals}
          >
            <Title level={3}>结果图表</Title>
            {echartsOption ? <ReactECharts option={echartsOption} /> : <div>正在解析</div>}
            <Divider style={{ margin: '8% auto 4% auto' }} />
            <Title level={3}>分析结论</Title>
            <Paragraph style={{ whiteSpace: 'pre-wrap' }}>
              {chartVals?.genText || 'genText'}
            </Paragraph>
          </Card>
        </Col>
      </Row>
    </div>
  );
};
export default ChartResult;
