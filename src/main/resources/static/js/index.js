let nextbut = document.getElementById("next");
let wenzi = document.getElementById("wenzi");
let login = document.querySelectorAll(".login")[1];
let fanhui = document.getElementById("fanhui");
let registerForm = document.getElementById("register-form");
let registerFanhui = document.getElementById("register_fanhui");
let bg = document.getElementById("loginleft");
let wangji = document.getElementById("wangji");
let rwenzi = document.getElementById("register_wenzi");
let register = document.getElementById("register");
let username = document.getElementById("username");
let rememberP = document.getElementById("rem");
let password = document.getElementById("password");

//屏蔽tab
document.onkeydown = onkeydownTab;
function onkeydownTab() {
    if (event.keyCode === 9) {
        return false;
    }
}

//添加enter事件
document.getElementById("username").onkeydown = keyListener;

function keyListener(e) {
    if (e.keyCode === 13) {
        if (document.activeElement.id === "username")
            nextbut.click();
        else if (document.activeElement.id === "password") {
            document.getElementById("login").click();
        }else
            document.getElementById("register").click();
        return false;
    }
}

document.getElementById("login").addEventListener("click", function () {
    if (rememberP.checked) {
        console.log("======记住了用户名和密码======");
        localStorage.setItem('password', password.value);
        localStorage.setItem('username',username.value)
    }
});

rememberP.addEventListener('change',function () {
    if (!this.checked){
        localStorage.removeItem('username')
        localStorage.removeItem('password')
    }
})

//验证账号
nextbut.addEventListener("click", function () {
    let username = document.getElementById("username").value;
    if (check(username)) {
        toLogin();
    } else {
        document.getElementById("register_username").value = username;

        rwenzi.innerText = "注册";
        register.innerText = "注册";
        document.getElementById("reg_pwd").innerText = "密码";
        document.getElementById("register-form").action = "/register";

        toRegister();
    }
});

//验证主要内容
function check(username) {
    let result = false;
    $.ajax({
        url: '/doesUserExist',
        method: 'get',
        async: false,
        data: {
            "username": username
        },
        success: (data) => {
            let jsonDate = JSON.parse(data);
            console.log(jsonDate);
            result = (jsonDate.code === 1);
        }
    })
    return result;
}

//注册的返回按钮
registerFanhui.onclick = function (e) {
    e.preventDefault();
    registerForm.setAttribute("class", "");
    bg.setAttribute("class", "");
    document.onkeydown = onkeydownTab;
    document.getElementById("username").onkeydown = keyListener;
}

//登录的返回按钮
fanhui.onclick = function () {
    document.getElementById("password").value = "";
    let className = document.querySelectorAll(".login")[1].getAttribute("class");
    if (className.search("flag2") !== -1) {
        login.removeAttribute("class");
        login.setAttribute("class", "login flag1");
    }
    wenzi.innerText = "登录/注册";
    document.onkeydown = onkeydownTab;
    document.getElementById("username").onkeydown = keyListener;
    return false;
}

//登录
function toLogin() {
    console.log("进入了toLogin方法");
    if (username.value === localStorage.getItem('username')){
        console.log("===保存的用户名与输入的一致===")
        password.value=(localStorage.getItem('password'))
        rememberP.checked=true
    }
    document.getElementById("password").onkeydown = keyListener;
    let className = document.querySelectorAll(".login")[1].getAttribute("class");
    if (className.search("flag2") === -1) {
        login.setAttribute("class", className + " flag2");
    }
    wenzi.innerText = "请输入密码";
    document.onkeydown = null;
    document.getElementById("password").focus();
    console.log("toLogin方法执行结束");
}

//注册
function toRegister() {
    registerForm.setAttribute("class", "register");
    bg.setAttribute("class", "register2");
    document.onkeydown = null;
    document.getElementById("register_password").focus();
    document.getElementById("register_username").onkeydown = keyListener;
    document.getElementById("register_password").onkeydown = keyListener;
    document.getElementById("register_mail").onkeydown = keyListener;
    document.getElementById("code").onkeydown = keyListener;

}

// 发送验证码
$("#send_code").on('click', function (e) {
    e.preventDefault();
    $(this).attr("disabled", "disabled")
    $.ajax({
        type: "get",
        url: "/sendcode",
        data: {
            mail: $("#register_mail").val()
        },
        success: function (data) {
            console.log(data);
        },
        error: function (data) {
            console.log(data);
        }
    })
    let seconds = 60;
    let btn_code = $("#send_code");
    let interval = setInterval(function () {
        if (seconds <= 0) {
            btn_code.text("发送验证码")
            btn_code.removeAttr("disabled")
            clearInterval(interval)
        } else {
            btn_code.text(seconds + "秒后再发");
            seconds--;
        }
    }, 1000)

})

wangji.onclick = function toReset(e){
    document.getElementById("register_username").value = document.getElementById("username").value;
    rwenzi.innerText = "重置密码";
    register.innerText = "重置";
    document.getElementById("reg_pwd").innerText = "新密码";
    document.getElementById("register-form").action = "/resetPassword";
    toRegister();

}