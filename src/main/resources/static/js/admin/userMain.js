
//移到回收站提交按钮
function deleteByAdmin() {
    $("#deleteCancel").click();
    $(".fullpage-wrapper").fadeIn(300);
    let filenames = [];
    filenames.push($('#name').val());
    let imagesId = [];
    imagesId.push($('#imagesId').val());
    console.log(filenames.toString());
    $.ajax({
        type: "post",
        url: "/admin/delete",
        data: {
            imagesId: imagesId.toString()
        },
        success: function (data) {
            window.open("userMain","_parent");
        },
        error: function (data) {
            console.log(data);
        }
    })

}

//删除文件
function delete_file(name) {
    $("#name").val(name);
    $("#deleteFileName").text(name);
}

//批量删除
$("#toolItems-trash").on("click", deleteFile);
$("#toolItems-pass").on("click", deleteFile);


function deleteFile() {
    let valArray = [];
    let srcArrauy = [];
    let idArray = [];
    let val;
    let deleteFileNameHtml = "";
    $('input[name="albumPicture"]:checked').each(function () {
        idArray.push($(this).prev().val());
        val = $(this).val();
        srcArrauy.push($(this).next().find("img")[0].src);
        valArray.push(val);
    })

    $("#name").val(valArray);
    $("#imagesId").val(idArray);
    for (let i = 0; i < valArray.length; i++) {
        deleteFileNameHtml += "<div><img src='" + srcArrauy[i] + "'><p>" + valArray[i] + "</p></div>";
    }
    $("#deleteFileName").html(deleteFileNameHtml);
}

//下拉菜单
let flag = false;
let menuBtn = $("#container2-menu");
let mobileMenu = $("#menu-content");

menuBtn.click(function () {
    if (mobileMenu.hasClass("menu-show")) {
        hideMenu()
        flag = false
    } else {
        flag = true;
        showMenu()
    }
})

function showMenu() {
    mobileMenu.addClass("menu-show").removeClass("menu-hide");
}

function hideMenu() {
    mobileMenu.addClass("menu-hide").removeClass("menu-show");
}



function imageInformation() {
    $(".detailed_information").css({"opacity": "1", "z-index": "2015"});
    var viewer = new Viewer(document.getElementById('dowebok'), {
        url: 'original'
    });
    showImgMation();
}

function showImgMation() {
    // console.log(event.target.alt);
    //获取当前选中对象的属性值
    showImgInformation();
}

function showImgNextPrev() {
    showImgInformation();
}


function showImgInformation() {
    console.log("进入了userMain.js的showImgInformation()方法");
    // $(".viewer-container").css({"width": "100%"});
    // $(".viewer-transition.viewer-move").css({"left": "0"})

}

function closeImg() {
    $(".detailed_information").css({"opacity": "0", "z-index": "-5"});
}


function deleteButton(){
    let deleteMessage = document.getElementById("deleteMessage");
    let deleteButton = document.getElementById("deleteButton");
    deleteMessage.innerText = "是否将以下文件彻底删除";
    deleteButton.onclick = deleteByAdmin;
    deleteButton.innerText = "删除";
}

function passButton(){
    let deleteMessage = document.getElementById("deleteMessage");
    let deleteButton = document.getElementById("deleteButton");
    deleteMessage.innerText = "是否审核通过以下文件";
    deleteButton.onclick = passByAdmin;
    deleteButton.innerText = "通过";
}



//移到回收站提交按钮
function passByAdmin() {
    $("#deleteCancel").click();
    $(".fullpage-wrapper").fadeIn(300);
    let filenames = [];
    filenames.push($('#name').val());
    let imagesId = [];
    imagesId.push($('#imagesId').val());
    console.log(filenames.toString());
    $.ajax({
        type: "post",
        url: "/admin/passFiles",
        data: {
            imagesId: imagesId.toString()
        },
        success: function (data) {
            window.open("userMain","_parent");
        },
        error: function (data) {
            console.log(data);
        }
    })

}

//拉取文件
function CheckNow(){
    // $("#checkCheck").click();
    $(".fullpage-wrapper").fadeIn(300);
    let filenames = [];
    filenames.push($('#name').val());
    let imagesId = [];
    imagesId.push($('#imagesId').val());
    console.log(filenames.toString());
    $.ajax({
        type: "post",
        url: "/imageCheck/check",
        data: {
            imagesId: imagesId.toString()
        },
        success: function (json) {
            $(".fullpage-wrapper").fadeOut(50);
            let data = JSON.parse(json)
            if(data.code===1){
                swal({
                    title: "成功",
                    text: data.msg,
                    icon: "success",
                    button: {
                        value: "好的"
                    },
                }).then((value) => {
                    window.open("userMain","_parent");
                })
            } else {
                let msg = data.msg;
                console.log(msg);
                swal({
                    title:"警告",
                    text: data.msg,
                    icon:"error",
                    button: "我知道了",
                });
            }

        },
        error: function () {
            $(".fullpage-wrapper").fadeOut(50);
            swal("错误", "出错了，请联系管理员！", "error");
        }
    })

}