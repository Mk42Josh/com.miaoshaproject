package org.example.response;

public class CommonReturnType {
    //表明对应请求的返回处理结果是success还是fail
    private String status;
    //成功则返回前端需要的json数据；失败则使用通用格式的错误码
    private Object data;

    //通用的创建方法
    public static CommonReturnType create(Object result){
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status){
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
