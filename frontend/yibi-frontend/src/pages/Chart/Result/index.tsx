import { getChartVoByIdUsingGet } from '@/services/yibi-frontend/chartController';
import { useModel } from '@@/exports';
import { DotChartOutlined } from '@ant-design/icons';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { sleep } from '@antfu/utils';
import { history } from '@umijs/max';
import { Col, message, Row, Skeleton } from 'antd';
import Title from 'antd/es/typography/Title';
import Paragraph from 'antd/lib/typography/Paragraph';
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
  const [chartVals, setChartVals] = useState();
  const [isFinished, setIsFinished] = useState();

  const { currentUser } = initialState;

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
        setIsFinished(0);
        setLoading(false);
        setChartVals(res.data);
        return;
      } else {
        await sleep(3000);
        if(!isFinished) {
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

  if (!initialState) {
    return loading;
  }

  if (!currentUser) {
    history.push(`/user/login?redirect=/chart/result/${id}`);
    return undefined;
  }

  return (
    <div className={chartResultClass}>
      {!chartVals ? (
        <Row>
          <Col span={10} style={{ fontSize: 40, color: '#bfbfbf' }}>
            <Skeleton.Input active={true} size={'large'} />
            <br />
            <Skeleton.Input active={true} size={'default'} />
            <br />
            <Skeleton paragraph={{ rows: 4 }} style={{ marginTop: 60 }} />
          </Col>
          <Col span={14} style={{ fontSize: 40, color: '#bfbfbf' }}>
            <Skeleton.Node active>
              <DotChartOutlined style={{ fontSize: 40, color: '#bfbfbf' }} />
            </Skeleton.Node>
            <Skeleton paragraph={{ rows: 5 }} style={{ marginTop: 60 }} />
          </Col>
        </Row>
      ) : (
        <Row>
          <Col span={10} style={{ fontSize: 40, color: '#bfbfbf' }}>
            <Title>{chartVals?.title || 'title'}</Title>

            <Title level={2}>分析需求</Title>

            <Paragraph>{chartVals?.goal || 'goal'}</Paragraph>
          </Col>
          <Col span={14} style={{ fontSize: 40, color: '#bfbfbf' }}>
            <Title level={2}>结果图</Title>
            <div id="Echarts">{chartVals?.genCode || 'genCode'}</div>
            <Title level={2}>分析结论</Title>
            <Paragraph>{chartVals?.genText || 'genText'}</Paragraph>
          </Col>
        </Row>
      )}
    </div>
  );
};
export default ChartResult;
