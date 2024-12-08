const router = require('express').Router();
const controller = require('../controllers/file');

router.get('/system', controller.getResources);
router.get('/:id', controller.getById);

/*

URL gốc: /api/v1/file
Điều kiện: Tất cả các API đều cần xác thực người dùng

1. Lấy thông tin file
URL: /:id
Method: GET
Response:
  - 200: Object {
    _id: string,
    name: string,
    url: string,
    type: string,
    status: string,
    description: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

2. Lấy tài nguyên hệ thống
URL: /system
Method: GET
Query:
  - page: number
  - limit: number
  - type: string
Response:
  - 200: Object {
    hasMore: boolean,
    data: [
      Object {
        _id: string,
        name: string,
        url: string,
        type: string,
        status: string,
        description: string,
        createdAt: string,
        updatedAt: string
      }
    ]
  }
  - 500: Object { message: string }

*/

module.exports = router;