// @ts-ignore
/* eslint-disable */
import { request } from '@umijs/max';

/** listObjectByPage POST /api/search/list */
export async function listObjectByPageUsingPost(
  body: API.CommonQueryRequest,
  options?: { [key: string]: any },
) {
  return request<API.BaseResponseIPage_>('/api/search/list', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    data: body,
    ...(options || {}),
  });
}
