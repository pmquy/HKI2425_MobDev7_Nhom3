const router = require('express').Router();
const controller = require('../controllers/friend');

router.post('/request', controller.request);
router.post('/accept', controller.accept);
router.post('/revoke', controller.revoke);
router.post('/decline', controller.decline);
router.post('/disfriend', controller.disfriend);
router.get('/suggestions', controller.getSuggestions);
router.get('/', controller.getAll);

/*

URL gốc: /api/v1/friend
Điều kiện: Tất cả các API đều cần xác thực người dùng

1. Gửi lời mời kết bạn
URL: /request
Method: POST
Content-Type: application/json
Body:
- to: string, bắt buộc, id của người muốn kết bạn
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

2. Chấp nhận lời mời kết bạn
URL: /accept
Method: POST
Content-Type: application/json
Body:
- from: string, bắt buộc, id của người gửi lời mời
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

3. Thu hồi lời mời kết bạn
URL: /revoke
Method: POST
Content-Type: application/json
Body:
- to: string, bắt buộc, id của người muốn thu hồi lời mời
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

4. Từ chối lời mời kết bạn
URL: /decline
Method: POST
Content-Type: application/json
Body:
- from: string, bắt buộc, id của người gửi lời mời
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

5. Hủy kết bạn
URL: /disfriend
Method: POST
Content-Type: application/json
Body:
- from: string, bắt buộc, id của người muốn hủy kết bạn
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

6. Lấy danh sách bạn bè
URL: /
Method: GET
Query:
- offset: number mặc định 0
- limit: number, mặc định 10
- q: string, query tìm kiếm, mặc định {}
Response:
  - 200: Array [{ _id: string, from: string, to: string, status: string }]
  - 500: Object { message: string }

*/

module.exports = router;

