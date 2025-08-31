
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
    			    // [groupName, [list of button]]
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
    			fontNames: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋움체','바탕체'],
    			fontSizes: ['8','9','10','11','12','14','16','18','20','22','24','28','30','36','50','72'],
				height: 500,                 // 에디터 높이
				disableResizeEditor: true,
				minHeight: null,             // 최소 높이
				maxHeight: null,             // 최대 높이
				focus: true,                  // 에디터 로딩후 포커스를 맞출지 여부
				lang: "ko-KR",					// 한글 설정
				/*placeholder: '',	//placeholder 설정*/
				callbacks: {	//여기 부분이 이미지를 첨부하는 부분
					onImageUpload : function(files) {
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
                        // 이미지가 삭제되면 이 함수가 호출됩니다.
                        var imageUrl = $(target[0]).attr('src');
                        if (imageUrl) {
                            // 서버에 삭제 요청을 보냅니다.
                            deleteSummernoteImageFile(imageUrl);
                        }
                    }

				}
	});





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




