(function () {
    window.comicBaker = {};
    var $page = $('#page'),
        $img = $('<img />'),
        pageURLs = [],
        index = 0,
        $rightButton = $('#rightButton'),
        $leftButton = $('#leftButton');

    $rightButton.click(function () {
        showPage(++index);
        return false;
    });

    $leftButton.click(function () {
        showPage(--index);
        return false;
    });

    function showPage(i) {
        if (i < 0) {
            index = 0;
        } else if (i >= pageURLs.length) {
            index = pageURLs.length - 1;
        } else {
            $img.attr('src', pageURLs[i]);
            $page.append($img);
        }

        if (index === 0) {
            $leftButton.removeClass('green');
        } else if (index == 1) {
            $leftButton.addClass('green');
        }

        if (index === pageURLs.length - 1) {
            $rightButton.removeClass('green');
        } else if (index ==  pageURLs.length - 2) {
            $rightButton.addClass('green');
        }
    }

    comicBaker.read = function (bookId) {
        pageRoute.book.BookController.JSONpagesGET(bookId).ajax().then(JSON.parse).then(function (response) {
            pageURLs = response.urls;
            showPage(index);
        });
    };

    $(document).keydown(function (e) {
        if (e.keyCode == 37) {
            showPage(--index);
            return false;
        } else if (e.keyCode == 39) {
            showPage(++index);
            return false;
        }
    });
})();