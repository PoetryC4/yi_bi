import { chartStateOptions } from '@/components/Entity/Enum/ChartStateEnum';
import { getChartVoByIdUsingGet } from '@/services/yibi-frontend/chartController';
import { useModel } from '@@/exports';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { sleep } from '@antfu/utils';
import { history } from '@umijs/max';
import { Button, Card, Col, Divider, message, Result, Row } from 'antd';
import Title from 'antd/es/typography/Title';
import { Table } from 'antd/lib';
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
  const [chartDataColumns, setChartDataColumns] = useState<any>();
  const [chartDataSource, setChartDataSource] = useState<any>();
  const [hasError, setHasError] = useState(false);

  if (!initialState) {
    return loading;
  }

  const { currentUser } = initialState;

  if (!currentUser) {
    history.push(`/user/login?redirect=/chart/result/${id}`);
  }

  const handleCopyCase = () => {
    history.push(`/chart/add?copy=${id}`);
  };

  const handleGoToTable = () => {
    history.push(`/chart/table`);
  };

  const chartDataAnalysis = (chartData: string) => {
    const rows = chartData.split('\n');
    const headers = rows[0].split(',');
    const countOfCols = headers.length;
    const dataColumns = [];
    for (let i = 0; i < countOfCols; i++) {
      dataColumns.push({
        title: headers[i],
        dataIndex: 'idx_' + i.toString(),
        key: 'idx_' + i.toString(),
      });
    }
    setChartDataColumns(dataColumns);
    const dataSource_1 = [];
    for (let i = 1; i < rows.length; i++) {
      const curRow = rows[i].split(',');
      const curRowData = {};
      for (let j = 0; j < countOfCols; j++) {
        curRowData['idx_' + j.toString()] = curRow[j];
      }
      dataSource_1.push(curRowData);
    }
    setChartDataSource(dataSource_1);
  };

  const tryGetChart = async () => {
    let res = await getChartVoByIdUsingGet({
      id: id,
    });
    if (res.code === 0) {
      if (res.data.isFinished === chartStateOptions.finished) {
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
          const genCode = JSON.parse(refinedJsonStr);
          if (!genCode) {
            message.error('图表解析出错，请重试');
          } else {
            setChartVals(res.data);
            setEchartsOption(genCode);
            setIsFinished(0);
            chartDataAnalysis(res.data.chartData);
            setLoading(false);
          }
        }
        return;
      } else if (res.data.isFinished === chartStateOptions.waiting) {
        await sleep(3000);
        if (!isFinished) {
          await tryGetChart();
        }
        return;
      } else {
        setHasError(true);
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
      {hasError ? (
        <Result
          status="error"
          title="图表分析错误"
          subTitle="你可以重新进行分析或返回图标列表"
          extra={[
            <Button
              type="primary"
              onClick={() => handleCopyCase()}
              style={{ marginLeft: 20, marginRight: 20 }}
            >
              重新进行分析
            </Button>,
            <Button
              type="primary"
              onClick={() => handleGoToTable()}
              style={{ marginLeft: 20, marginRight: 20 }}
            >
              返回我的列表
            </Button>,
          ]}
        ></Result>
      ) : (
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
              extra={
                <Button type="primary" onClick={handleCopyCase}>
                  重新进行分析
                </Button>
              }
            >
              <Title level={3}>分析需求</Title>
              <Paragraph style={{ whiteSpace: 'pre-wrap' }}>{chartVals?.goal || 'goal'}</Paragraph>
              <Divider />
              <Title level={3}>原始数据</Title>
              <Paragraph style={{ whiteSpace: 'pre-wrap' }}>
                <Table dataSource={chartDataSource} columns={chartDataColumns} />
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
              <Divider />
              <Title level={3}>分析结论</Title>
              <Paragraph style={{ whiteSpace: 'pre-wrap' }}>
                {chartVals?.genText || 'genText'}
              </Paragraph>
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
};
export default ChartResult;
