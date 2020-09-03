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

<form action="/wavtotxt" method="POST" enctype="multipart/form-data">

    <div>
        SpeechToText : <input type="file" name="file" id="fileOpenInput"/>
        <input type="submit" value="전송">
    </div>
</form>

<form action="/uploadFile" method="POST" enctype="multipart/form-data">

    <div>
        파일 로컬 업로드: <input type="file" name="file" id="uploadFile"/>
        <input type="submit" value="전송">
    </div>
</form>

<form action="/GCUpload" method="POST" enctype="multipart/form-data">

    <div>
        파일 로컬 + 클라우드 업로드 : <input type="file" name="file" id="GCUpload"/>
        <input type="submit" value="전송">
    </div>
</form>

</body>
</html>