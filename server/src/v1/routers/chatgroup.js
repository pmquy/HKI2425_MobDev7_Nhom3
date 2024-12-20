const router = require('express').Router();
const controller = require('../controllers/chatgroup');
const { single } = require('../middlewares/multer');

router.post('/:id/member', controller.addMember);
router.delete('/:id/member', controller.removeMember);
router.put('/:id/member', controller.updateMember);
router.get('/:id/member', controller.getMember);
router.get('/:id/message', controller.getMessage);
router.put('/:id', ...single('avatar'), controller.update);
router.get('/:id', controller.getById);
router.delete('/:id', controller.deleteById);
router.post('/', ...single('avatar'), controller.create);
router.get('/', controller.getAll);

/*
 

URL gốc: /api/v1/chatgroup
Điều kiện: Tất cả các API đều cần xác thực người dùng

1. Tạo nhóm chat mới
URL: /
Method: POST
Content-Type: multipart/form-data
Body:
- name: string, bắt buộc
- avatar: file, mặc định là ảnh mặc định
- users: mảng các đối tượng
  + user: string, bắt buộc
  + role: string, mặc định là member
Response:
  - 200: Object {
    _id: string,
    name: string,
    avatar: string,
    users: array,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

2. Lấy danh sách nhóm chat
URL: /
Method: GET
Query:
- offset: number, mặc định là 0
- limit: number, mặc định là 10
- q: string, mặc định là '{}'
Response:
  - 200: Object {
    data: [Object {
      _id: string,
      name: string,
      avatar: string,
      users: array,
      createdAt: string,
      updatedAt: string
    }],
    hasMore: boolean
  }
  - 500: Object { message: string }


3. Lấy thông tin nhóm chat theo id
URL: /:id
Method: GET
Response:
  - 200: Object {
    _id: string,
    name: string,
    avatar: string,
    users: array,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

4. Cập nhật thông tin nhóm chat theo id
URL: /:id
Method: PUT
Content-Type: multipart/form-data
Body:
- name: string
- avatar: file
Response:
  - 200: Object {
    _id: string,
    name: string,
    avatar: string,
    users: array,
    createdAt: string,
    updatedAt: string
  }
  - 500: Object { message: string }

5. Xóa nhóm chat theo id
URL: /:id
Method: DELETE
Response:
  - 200: Object { message: string }
  - 500: Object { message: string }

6. Lấy danh sách thành viên của nhóm chat theo id
URL: /:id/member
Method: GET
Response:
  - 200: [Object {
    user: string
    role: string
  }]
  - 500: Object { message: string }

7. Thêm thành viên vào nhóm chat theo id
URL: /:id/member
Method: POST
Content-Type: application/json
Body: [
  Object {
    user: string,
    role: string
  }
]
Response:
  - 200: [
    Object {
      user: string
      role: string
    }
  ]
  - 500: Object { message: string }

8. Cập nhật quyền thành viên trong nhóm chat theo id

URL: /:id/member
Method: PUT
Content-Type: application/json
Body: [
  Object {
    user: string,
    role: string
  }
]

Response:
  - 200: [
    Object {
      user: string
      role: string
    }
  ]
  - 500: Object { message: string }

9. Xóa thành viên khỏi nhóm chat theo id
URL: /:id/member
Method: DELETE
Content-Type: application/json
Body: [string]
Response:
  - 200: [
    Object {
      user: string
      role: string
    }
  ]
  - 500: Object { message: string }


10. Lấy danh sách tin nhắn của nhóm chat theo id
URL: /:id/message
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
        chatgroup: string,
        user: string,
        message: string,
        files: [string]
        createdAt: string,
        updatedAt: string
      }
    ],
    hasMore: boolean
  }
  - 500: Object { message : string }
*/

module.exports = router;