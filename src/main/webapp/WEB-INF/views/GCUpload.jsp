<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Insert title here</title>
</head>
<body>
insert

<form action="/GCUpload" method="POST" enctype="multipart/form-data">

    <div>
        파일 : <input type="file" name="file" id="fileOpenInput"/>
        <input type="submit" value="전송">
    </div>
</form>
</body>
</html>