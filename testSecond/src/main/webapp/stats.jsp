<%@ page isELIgnored="false" %>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<html>
<head>
    <link rel="stylesheet" href="styles.css">
    <title>Иформация о файле ${fileStats.getFilename()}</title>
</head>
<body style="background-color: #333333">
    <div id="mainContainerInfo">
        <div id="showStatsDiv">
            <div>
                <p style="width: 20vw; align-content: center; text-align: center"><b>${fileStats.getFilename()}</b></p>
            </div>
            <div>
                <p style="width: 20vw; align-content: center; text-align: center">
                <form action="/download" style=" text-align: center;">
                    <input name="id" value="${fileStats.getFileId()}" hidden>
                    <button style="height: 3vh; width: 12vw;">Скачать</button>
                </form>
                </p>
            </div>
            <div style="display: flex;">
                <div>
                    <p style="width: 10vw; align-content: center; text-align: center">Просмотров:</p>
                    <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getViewsCount()}</p>
                </div>
                <div>
                    <p style="width: 10vw; align-content: center; text-align: center">Скачиваний:</p>
                    <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getDownloadsCount()}</p>
                </div>
            </div>

            <div style="display: flex;">
                <div>
                    <p style="width: 10vw; align-content: center; text-align: center">Загружен:</p>
                    <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getDisplayingLastDownloadTime()}</p>
                </div>
                <div>
                    <p style="width: 10vw; align-content: center; text-align: center">Будет удалён:</p>
                    <p style="width: 10vw; align-content: center; text-align: center">${fileStats.getDisplayingTerminationTime()}</p>
                </div>
            </div>

            <div style="text-align: center">
                <p style="width: 20vw; align-content: center; text-align: center">Последнее скачивание:</p>
                <p style="width: 20vw; align-content: center; text-align: center">${fileStats.getDisplayingLastDownloadTime()}</p>
            </div>
        </div>
    </div>
</body>

</html>