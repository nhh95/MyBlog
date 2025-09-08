
$(function(){
    var token  = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    if (token && header) {
        $.ajaxSetup({
            beforeSend: function(xhr){ xhr.setRequestHeader(header, token); }
        });
    }
});




$('#summernote').summernote({
			  toolbar: [
                    ['style', ['style']],
    			    ['fontname', ['fontname']],
    			    ['fontsize', ['fontsize']],
    			    ['style', ['bold', 'italic', 'underline','strikethrough', 'clear']],
    			    ['color', ['forecolor','color']],
    			    ['table', ['table']],
    			    ['para', ['ul', 'ol', 'paragraph']],
    			    ['height', ['height']],
    			    ['insert',['picture','link','video']],
    			    ['view', ['fullscreen', 'help']]
    			  ],
                styleTags: ['p','blockquote','h1', 'h2', 'h3','h4','h5','h6'],
    			fontNames: ['sans-serif','Arial', 'Arial Black', 'Comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋움체','바탕체'],
    			fontSizes: ['8','9','10','11','12','14','16','18','20','22','24','28','30','36','50','72'],
				height: 800,
				disableResizeEditor: true,
				minHeight: null,             // 최소 높이
				maxHeight: null,             // 최대 높이
				focus: true,                  // 에디터 로딩후 포커스를 맞출지 여부
				lang: "ko-KR",					// 한글 설정
				/*placeholder: '',	//placeholder 설정*/
				callbacks: {	//여기 부분이 이미지를 첨부하는 부분
					onImageUpload : function(files,editor,welEditable) {
						uploadSummernoteImageFile(files[0],this);
					},
					onPaste: function (e) {
						var clipboardData = e.originalEvent.clipboardData;
						if (clipboardData && clipboardData.items && clipboardData.items.length) {
							var item = clipboardData.items[0];
							if (item.kind === 'file' && item.type.indexOf('image/') !== -1) {
								e.preventDefault();
							}
						}
					},
                    onMediaDelete: function(target) {

                        var imageUrl = $(target[0]).attr('src');
                        if (imageUrl) {

                            deleteSummernoteImageFile(imageUrl);
                        }
                    },
                    onChange: function(contents, $editable) {
                        // blockquote 태그가 생성될 때마다 인라인 스타일 추가
                        var $blockquotes = $editable.find('blockquote:not([style])');
                        $blockquotes.attr('style', 'padding: 15px 20px; margin: 0 0 20px; border-left: 5px solid #000000; background-color: #f9f9f9; color: #666; font-style: italic;');

                        // 모든 table 태그에 보더 스타일 추가 (기존 스타일 보존)
                        var $tables = $editable.find('table');
                        $tables.each(function() {
                            var $table = $(this);
                            var existingStyle = $table.attr('style') || '';

                            // border-collapse가 없으면 추가
                            if (existingStyle.indexOf('border-collapse') === -1) {
                                existingStyle += '; border-collapse: collapse';
                            }
                            // width가 없으면 추가
                            if (existingStyle.indexOf('width') === -1) {
                                existingStyle += '; width: 100%';
                            }
                            // margin이 없으면 추가
                            if (existingStyle.indexOf('margin') === -1) {
                                existingStyle += '; margin: 10px 0';
                            }

                            $table.attr('style', existingStyle.replace(/^;\s*/, ''));
                        });

                        // 모든 td, th 태그에 보더 스타일 추가 (기존 스타일 보존)
                        var $tableCells = $editable.find('table td, table th');
                        $tableCells.each(function() {
                            var $cell = $(this);
                            var existingStyle = $cell.attr('style') || '';

                            // border가 없으면 추가
                            if (existingStyle.indexOf('border') === -1) {
                                existingStyle += '; border: 1px solid #ddd';
                            }
                            // padding이 없으면 추가
                            if (existingStyle.indexOf('padding') === -1) {
                                existingStyle += '; padding: 8px';
                            }
                            // text-align이 없으면 추가
                            if (existingStyle.indexOf('text-align') === -1) {
                                existingStyle += '; text-align: left';
                            }

                            $cell.attr('style', existingStyle.replace(/^;\s*/, ''));
                        });


                    }


                }
	});


$("div.note-editable").on('drop',function(e){
    for(i=0; i< e.originalEvent.dataTransfer.files.length; i++){
        uploadSummernoteImageFile(e.originalEvent.dataTransfer.files[i],$("#summernote")[0]);
    }
    e.preventDefault();
})



function uploadSummernoteImageFile(file, editor) {
		data = new FormData();
		data.append("file", file);
		$.ajax({
			data : data,
			type : "POST",
			url : "/uploadSummernoteImageFile",
			contentType : false,
			processData : false,
			success : function(data) {
            	//항상 업로드된 파일의 url이 있어야 한다.
				$(editor).summernote('insertImage', data.url);
			},
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Image upload failed:", textStatus, errorThrown);
            }
		});
	}

    // 서버에 이미지 삭제 요청을 보내는 함수
    function deleteSummernoteImageFile(imageUrl) {
        $.ajax({
            data: { imageUrl: imageUrl },
            type: "POST",
            url: "/deleteSummernoteImageFile", // 이미지 삭제를 처리할 서버 엔드포인트
            success: function(response) {
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.error("Image deletion failed:", textStatus, errorThrown);
            }
        });
    }

    // 글 작성 폼 검증 함수
    function validatePostForm() {
        // 제목 검증
        const title = document.querySelector('input[name="title"]')?.value.trim();
        if (!title) {
            alert('제목을 입력해주세요.');
            return false;
        }

        // Summernote 내용 검증
        const content = $('#summernote').summernote('code');
        const textContent = $('<div>').html(content).text().trim();

        // HTML 태그를 제거한 순수 텍스트가 비어있는지 확인
        if (!textContent || textContent === '') {
            alert('내용을 입력해주세요.');
            $('#summernote').summernote('focus'); // 에디터에 포커스
            return false;
        }

        // 내용이 너무 짧은지 확인 (선택사항)
        if (textContent.length < 1) {
            alert('내용을 1자 이상 입력해주세요.');
            $('#summernote').summernote('focus');
            return false;
        }

        return true; // 검증 통과
    }

    // 드롭다운 메뉴 기능
    document.addEventListener('DOMContentLoaded', function() {
        const navHeaders = document.querySelectorAll('.nav-section h3');

        navHeaders.forEach(header => {
            header.addEventListener('click', function() {
                if (window.innerWidth <= 959) {
                    const nextUl = this.nextElementSibling;
                    if (nextUl && nextUl.tagName === 'UL') {
                        this.classList.toggle('active');
                        nextUl.classList.toggle('active');
                    }
                }
            });
        });

        window.addEventListener('resize', function() {
            if (window.innerWidth > 959) {
                navHeaders.forEach(header => header.classList.remove('active'));
                document.querySelectorAll('.nav-section ul').forEach(ul => ul.classList.remove('active'));
            }
        });
    });




