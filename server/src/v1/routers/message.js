const router = require('express').Router();
const controller = require('../controllers/message');
const { array } = require('../middlewares/multer');

router.put('/:id', ...array('files'), controller.updateById);
router.delete('/:id', controller.deleteById);
router.get('/:id', controller.getById)
router.post('/', ...array('files'), controller.create);
router.get('/', controller.getAll);


/*
URL gốc: /api/v1/message
Điều kiện: Tất cả các API đều cần xác thực người dùng

1. Lấy danh sách tin nhắn
URL: /
Method: GET
Query:
- offset: number, mặc định là 0
- limit: number, mặc định là 10
- q: string, mặc định là '{}'
Response:
  - 200: Object {
    data: [
      Object {
        _id: string,
        message: string,
        files: array,
        user: string,
        chatgroup: string,
        createdAt: string,
        updatedAt: string
      }
    ],
    hasMore: boolean
  }
  - 500: Object { message: string }

2. Tạo tin nhắn
URL: /
Method: POST
Content-Type: multipart/form-data
Body:
- message: string, bắt buộc
- files: mảng các file
- chatgroup: string, bắt buộc
Response:
  - 200: Object {
    _id: string,
    message: string,
    chatgroup: string,
    files: array,
    user: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }


3. Lấy thông tin tin nhắn theo id
URL: /:id
Method: GET
Response:
  - 200: Object {
    _id: string,
    message: string,
    files: array,
    user: string,
    chatgroup: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

4. Cập nhật thông tin tin nhắn theo id
URL: /:id
Method: PUT
Content-Type: multipart/form-data
Body:
- message: string
- files: mảng các file
Response:
  - 200: Object {
    _id: string,
    message: string,
    chatgroup: string,
    files: array,
    user: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

5. Xóa tin nhắn theo id
URL: /:id
Method: DELETE
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

*/

module.exports = router;