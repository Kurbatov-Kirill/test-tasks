<%@ page isELIgnored="false" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<body style="background-color: #333333">
    <div style="display: flex; flex-wrap: wrap; justify-self: center;
    height: 25vw; width: 20vw;
    margin-left: 25vw; margin-right: 25vw; margin-top: 5vw; margin-bottom: 5vw;
    padding: 5vw;
    background-color: #DDDDDD">
        <div>
            <p style="width: 20vw; align-content: center; text-align: center"><b>${fileStats.getFilename()}</b></p>
        </div>
        <div>
            <p style="width: 20vw; align-content: center; text-align: center">
                <form action="/download">
                    <input name="id" value="${fileStats.getFileId()}" hidden>
                    <button style="height: 5vw; width: 20vw;">Скачать</button>
                </form>
            </p>
        </div>
        <div>
            <p style="width: 10vw; align-content: center; text-align: center">Просмотров:</p>
            <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getViewsCount()}</p>
        </div>
        <div>
            <p style="width: 10vw; align-content: center; text-align: center">Скачиваний:</p>
            <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getDownloadsCount()}</p>
        </div>
        <div>
            <p style="width: 10vw; align-content: center; text-align: center">Загружен:</p>
            <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getDisplayingLastDownloadTime()}</p>
        </div>
        <div>
            <p style="width: 10vw; align-content: center; text-align: center">Будет удалён:</p>
            <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getDisplayingTerminationTime()}</p>
        </div>
        <div style="text-align: center">
            <p style="width: 20vw; align-content: center; text-align: center">Последнее скачивание:</p>
            <p style="width: 20vw; align-content: center; text-align: center">${fileStats.getDisplayingLastDownloadTime()}</p>
        </div>
    </div>
</body>

</html>