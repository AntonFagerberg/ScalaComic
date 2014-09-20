(function () {
    var dropZone = $('#dropZone');

    dropZone.on('dragexit', function (e) {
        dropZone.removeClass('hover');
        e.preventDefault();
        e.stopPropagation();
    });

    dropZone.on('dragenter', function (e) {
        dropZone.addClass('hover');
        e.preventDefault();
        e.stopPropagation();
    });

    dropZone.on('drop', function (e) {
        if (e.originalEvent.dataTransfer && e.originalEvent.dataTransfer.files.length) {
            e.preventDefault();
            e.stopPropagation();

            var data = new FormData(),
                jsRoute = jsRoutes.book.BookController.uploadPOST();

            jQuery.each(e.originalEvent.dataTransfer.files, function(i, file) {
                data.append('file-' + i, file);
            });

            $.ajax({
                url: jsRoute.url,
                data: data,
                cache: false,
                contentType: false,
                processData: false,
                type: 'POST'
            });
        }
    });
})();