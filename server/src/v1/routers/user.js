const router = require('express').Router();
const controller = require('../controllers/user');
const { single } = require('../middlewares/multer');
const auth = require('../middlewares/auth');

router.post('/login', controller.login);
router.post('/otp', controller.otp);
router.post('/logout', auth({ required: true }), controller.logout);
router.get('/auth', auth({ required: true }), controller.auth);
router.get('/:id', auth({ required: true }), controller.getById);
router.get('/', auth({ required: true }), controller.getAll);
router.post('/', ...single('avatar'), controller.create);
router.put('/', auth({ required: true }), ...single('avatar'), controller.update);
router.delete('/', auth({ required: true }), controller.delete);


/*

URL gốc: /api/v1/user

1. Đăng nhập
URL: /login
Method: POST
Content-Type: application/json
Body:
- email: string, bắt buộc
- password: string, bắt buộc
Response:
  - 200: Object {
    _id: string,
    firstName: string,
    lastName: string,
    phoneNumber: string,
    email: string,
    avatar: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

2. Đăng xuất
URL: /logout
Method: POST
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

3. Xác thực
URL: /auth
Method: GET
Query:
  - token: string, bắt buộc (FCM token)
  - socketId: string, bắt buộc (Socket ID)
Response:
  - 200: Object {
    _id: string,
    firstName: string,
    lastName: string,
    phoneNumber: string,
    email: string,
    avatar: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

4. Lấy thông tin người dùng theo id
URL: /:id
Method: GET
Response:
  - 200: Object {
    _id: string,
    firstName: string,
    lastName: string,
    phoneNumber: string,
    email: string,
    avatar: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

5. Lấy danh sách người dùng
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
        firstName: string,
        lastName: string,
        phoneNumber: string,
        email: string,
        avatar: string,
        createdAt: string,
        updatedAt: string
      }
    ],
    hasMore: boolean
  }
  - 500: Object { message: string }

6. Tạo người dùng
URL: /
Method: POST
Content-Type: multipart/form-data
Body:
- firstName: string, bắt buộc
- lastName: string, bắt buộc
- phoneNumber: string, bắt buộc
- email: string, bắt buộc
- password: string, bắt buộc
- avatar: file, mặc định là ảnh mặc định
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

7. Cập nhật thông tin người dùng
URL: /
Method: PUT
Content-Type: multipart/form-data
Body:
- firstName: string
- lastName: string
- phoneNumber: string
- password: string
- avatar: file
Response:
  - 200: Object {
    _id: string,
    firstName: string,
    lastName: string,
    phoneNumber: string,
    email: string,
    avatar: string,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

8. Xóa người dùng
URL: /
Method: DELETE
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

9. Xác thực OTP
URL: /otp
Method: POST
Content-Type: application/json
Body:
- email: string, bắt buộc
- otp: number, bắt buộc
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }
*/


module.exports = router;

