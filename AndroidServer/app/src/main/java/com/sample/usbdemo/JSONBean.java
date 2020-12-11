package com.sample.usbdemo;

import com.sample.usbdemo.dao.User;

import java.util.List;

public class JSONBean {
    /**
     * address : {}
     * user : {"user_version":1,"user_list":[{"sex":"男","name":"张三","age":29},{"sex":"女","name":"李思","age":32},{"sex":"男","name":"王五","age":23},{"sex":"女","name":"赵柳","age":19}]}
     */
    private AddressEntity address;
    private UserEntity user;

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public AddressEntity getAddress() {
        return address;
    }

    public UserEntity getUser() {
        return user;
    }

    public class AddressEntity {
    }

    public class UserEntity {
        /**
         * user_version : 1
         * user_list : [{"sex":"男","name":"张三","age":29},{"sex":"女","name":"李思","age":32},{"sex":"男","name":"王五","age":23},{"sex":"女","name":"赵柳","age":19}]
         */
        private int user_version;
        private List<User> user_list;

        public void setUser_version(int user_version) {
            this.user_version = user_version;
        }

        public void setUser_list(List<User> user_list) {
            this.user_list = user_list;
        }

        public int getUser_version() {
            return user_version;
        }

        public List<User> getUser_list() {
            return user_list;
        }
    }
}
