package io.ttyys.test.invoke

import javax.validation.constraints.NotBlank

/**
 * 用户
 */
class User {

    @NotBlank(message = "用户ID不可为空")
    String id

    @NotBlank(message = "姓名不可为空")
    String name

    String sex
}

/**
 * 账户
 */
class Account {
    String id

    @NotBlank(message = "姓名不可为空")
    String username

    String account

    String password
}
