import { listObjectByPageUsingPost } from '@/services/yibi-frontend/searchController';
import { useModel } from '@@/exports';
import { useEmotionCss } from '@ant-design/use-emotion-css';
import { history } from '@umijs/max';
import { Button, Divider, Input, message, Pagination } from 'antd';
import Paragraph from 'antd/es/typography/Paragraph';
import Title from 'antd/es/typography/Title';
import { Table } from 'antd/lib';
import { ColumnsType } from 'antd/lib/table';
import moment from 'moment';
import React, { useEffect, useState } from 'react';

const handleCopyCase = (chartId: string) => {
  history.push(`/chart/add?copy=${chartId}`);
};

const handleGoToCase = (chartId: string) => {
  history.push(`/chart/result/${chartId}`);
};

const ChartTable: React.FC = () => {
  const [userLoginState, setUserLoginState] = useState<API.LoginResult>({});
  const [type, setType] = useState<string>('account');
  const { initialState, setInitialState } = useModel('@@initialState');
  const chartTableClass = useEmotionCss(() => {
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
  const [chartTable, setChartTable] = useState<API.ChartVO[]>([]);
  const [chartCount, setChartCount] = useState(0);
  const [searchInput, setSearchInput] = useState('');
  const [curPage, setCurPage] = useState(1);
  const [pageSize, setPageSize] = useState(8);
  const pageSizes = [8, 16, 24];

  if (!initialState) {
    return loading;
  }

  const { currentUser } = initialState;

  if (!currentUser) {
    history.push('/user/login?redirect=/chart/table');
  }

  const getUserCharts = async () => {
    const res = await listObjectByPageUsingPost({
      pageSize: pageSize.toString(),
      current: curPage.toString(),
      useEs: false,
      category: 'chart',
      searchText: searchInput,
    });
    if (res.code === 0) {
      setChartTable(res.data.records);
      setChartCount(res.data.total);
      message.info('获取成功');
    } else {
      message.error(res.message);
    }
  };

  const handleSearchChange = async (e) => {
    // 更新状态的值
    setSearchInput(e.target.value);
  };

  const handlePaginationChange = async (page: number, pageSize: number) => {
    setCurPage(page);
    setPageSize(pageSize);
  };

  useEffect(() => {
    handlePaginationChange(1, 8);
    getUserCharts();
  }, [pageSize, curPage, searchInput]);

  const columns: ColumnsType<API.ChartVO> = [
    { title: '标题', dataIndex: 'title', key: 'title' },
    { title: '图表类型', dataIndex: 'chartType', key: 'chartType' },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (text, record) => <span>{moment(text).format('YYYY-MM-DD')}</span>,
    },
    {
      title: '操作',
      dataIndex: '',
      key: 'title',
      render: (text, record) => (
        <div>
          <Button
            type="primary"
            onClick={() => handleCopyCase(record.id || '0')}
            style={{ marginLeft: 20, marginRight: 20 }}
          >
            重新进行分析
          </Button>
          <Button
            type="primary"
            onClick={() => handleGoToCase(record.id || '0')}
            style={{ marginLeft: 20, marginRight: 20 }}
          >
            查看分析结果
          </Button>
        </div>
      ),
    },
  ];
  return (
    <div className={chartTableClass}>
      <div
        style={{
          width: '60%',
          inset: 0,
          margin: '0 auto auto auto',
        }}
      >
        <Input
          placeholder="搜索输入"
          value={searchInput}
          onChange={handleSearchChange}
          style={{ width: '30%', margin: '1% 40% 1% 10%' }}
        />
        <Table
          rowKey="id"
          pagination={false}
          columns={columns}
          expandable={{
            expandedRowRender: (record) => (
              <p style={{ margin: 0 }}>
                {
                  <div>
                    <Title level={4}>分析需求</Title>
                    <Paragraph>{record.goal}</Paragraph>
                    <Divider />
                    <Title level={4}>结论文字</Title>
                    <Paragraph>{record.genText || '结论文字'}</Paragraph>
                  </div>
                }
              </p>
            ),
            rowExpandable: (record) => record.goal !== undefined,
          }}
          dataSource={chartTable}
        />
        <Pagination
          style={{ inset: 0, margin: '1% auto auto auto' }}
          showSizeChanger
          total={chartCount}
          showTotal={(total, range) =>
            `${total} 个结果中的 ${(curPage - 1) * pageSize + 1}-${Math.min(
              curPage * pageSize,
              total,
            )} `
          }
          defaultPageSize={pageSize}
          defaultCurrent={curPage}
          pageSizeOptions={pageSizes}
          onChange={handlePaginationChange}
        />
      </div>
    </div>
  );
};
export default ChartTable;
