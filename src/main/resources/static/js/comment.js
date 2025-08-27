// 페이지 로드 시 댓글 목록 불러오기
$(document).ready(function() {
/*    // CSRF 토큰 설정
    var token = $("meta[name='_csrf']").attr("content");
    var header = $("meta[name='_csrf_header']").attr("content");

    $.ajaxSetup({
        beforeSend: function (xhr) {
            const tokenEl = document.querySelector("meta[name='_csrf']");
            const headerEl = document.querySelector("meta[name='_csrf_header']");
            if (tokenEl && headerEl) {
                xhr.setRequestHeader(headerEl.getAttribute("content"), tokenEl.getAttribute("content"));
            } else {
                // 메타 없으면 콘솔에 경고
                console.warn("CSRF 메타 태그가 없습니다. (헤더 미설정)");
            }
        }
    });*/

    loadComments();
    loadCommentCount();
});

// 댓글 생성 (함수명 변경)
function saveNewComment() {
    const commentContent = document.getElementById('commentContent').value.trim();
    const postId = parseInt(document.getElementById('postId').value, 10);
    const userEmail = document.getElementById('user-email').value;

    if (!commentContent) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    const data = {
        commentContent: commentContent,
        postId: postId,
        userEmail: userEmail
    };

    $.ajax({
        url: '/api/comments',
        method: 'POST',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (response) {
            if (response.success) {
                alert('댓글이 저장되었습니다.');
                $('#commentContent').val('');
                loadComments();
                loadCommentCount();
            } else {
                alert('저장 실패: ' + response.message);
            }
        },
        error: function (xhr, status, error) {
            alert('저장 실패: ' + error);
        }
    });
}

//비회원 댓글 저장
function saveGuestComment() {
    const nickname = document.getElementById('guestNickname').value.trim();
    const password = document.getElementById('guestPassword').value.trim();
    const content = document.getElementById('commentContent').value.trim();
    const postId = document.getElementById('postId').value;

    if (!nickname) {
        alert('닉네임을 입력해주세요.');
        return;
    }
    if (!password) {
        alert('비밀번호를 입력해주세요.');
        return;
    }
    if (!content) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    const requestData = {
        commentContent: content,
        postId: parseInt(postId),
        guestNickname: nickname,
        guestPassword: password,
        isGuest: true
    };

 /*   // CSRF 토큰 가져오기
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
*/

    fetch('/api/comments', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json',
           /* [header]: token  // CSRF 헤더 추가*/
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('댓글이 작성되었습니다.');
                // 폼 초기화
                document.getElementById('guestNickname').value = '';
                document.getElementById('guestPassword').value = '';
                document.getElementById('commentContent').value = '';
                // 댓글 목록 새로고침
                loadComments();
            } else {
                alert('댓글 작성 실패: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('댓글 작성 중 오류가 발생했습니다.');
        });
}


// 댓글 목록 조회
function loadComments() {
    const postId = document.getElementById('postId').value;

    $.ajax({
        url: `/api/comments/post/${postId}`,
        method: 'GET',
        success: function (comments) {
            displayComments(comments);
        },
        error: function (xhr, status, error) {
            console.error('댓글 조회 실패:', error);
        }
    });
}

// 댓글 목록 표시
function displayComments(comments) {
    const commentList = document.getElementById('commentList');
    const currentUserEmail = document.getElementById('user-email').value;



    if (comments.length === 0) {
        commentList.innerHTML = '<p>등록된 댓글이 없습니다.</p>';
        return;
    }

    let html = '';
    comments.forEach(comment => {
        let actionButtons = '';

        // 비회원 댓글인지 확인
        if (comment.isGuestComment) {
            // 비회원 댓글은 누구나 비밀번호로 수정/삭제 가능
            actionButtons = `
                <button onclick="editGuestComment(${comment.id})" class="comment-action-btn">수정</button>
                <button onclick="deleteGuestComment(${comment.id})" class="comment-action-btn">삭제</button>
            `;
        } else {
            // 회원 댓글은 본인만 수정/삭제 가능
            const isOwner = currentUserEmail === comment.userEmail;
            if (isOwner) {
                actionButtons = `
                    <button onclick="editComment(${comment.id})" class="comment-action-btn">수정</button>
                    <button onclick="deleteComment(${comment.id})" class="comment-action-btn">삭제</button>
                `;
            } else {
                actionButtons = '<span class="comment-no-permission">수정/삭제 권한이 없습니다.</span>';
            }
        }




        let editFormHtml = '';
        if (comment.isGuestComment) {
            editFormHtml = `
                <div class="comment-edit-form hidden" id="edit-form-${comment.id}">
                    <div style="margin-bottom: 10px;">
                        <input type="password" id="edit-password-${comment.id}" placeholder="비밀번호 입력" style="width: 200px;" />
                    </div>
                    <textarea id="edit-content-${comment.id}" class="comment-edit-textarea">${comment.commentContent}</textarea>
                    <button onclick="updateGuestComment(${comment.id})" class="comment-edit-btn">수정 완료</button>
                    <button onclick="cancelEdit(${comment.id})" class="comment-cancel-btn">취소</button>
                </div>
            `;
        } else {
            editFormHtml = `
                <div class="comment-edit-form hidden" id="edit-form-${comment.id}">
                    <textarea id="edit-content-${comment.id}" class="comment-edit-textarea">${comment.commentContent}</textarea>
                    <button onclick="updateComment(${comment.id})" class="comment-edit-btn">수정 완료</button>
                    <button onclick="cancelEdit(${comment.id})" class="comment-cancel-btn">취소</button>
                </div>
            `;
        }

        html += `
            <div class="comment-item" data-comment-id="${comment.id}">
                <div class="comment-header">
                    <strong>닉네임: ${comment.userNickname}</strong>
                    <div>
                        <span class="comment-date">작성일: ${comment.createdAt}</span>
                        <span class="comment-date">수정일: ${comment.updatedAt}</span>
                    </div>
                </div>
                <div class="comment-content" id="content-${comment.id}">
                    ${comment.commentContent}
                </div>
                ${editFormHtml}
                <div class="comment-actions" id="comment-actions-${comment.id}">
                    ${actionButtons}
                </div>
            </div>
        `;
    });


    commentList.innerHTML = html;
}


// 댓글 수정 폼 표시
function editComment(commentId) {
    document.getElementById(`content-${commentId}`).style.display = 'none';
    document.getElementById(`edit-form-${commentId}`).classList.remove('hidden');
    document.getElementById(`comment-actions-${commentId}`).classList.add('hidden');

}

// 비회원 댓글 수정 폼 표시
function editGuestComment(commentId) {
    document.getElementById(`content-${commentId}`).style.display = 'none';
    document.getElementById(`edit-form-${commentId}`).classList.remove('hidden');
    document.getElementById(`comment-actions-${commentId}`).classList.add('hidden');
}


// 댓글 수정 취소
function cancelEdit(commentId) {
    document.getElementById(`content-${commentId}`).style.display = 'block';
    document.getElementById(`edit-form-${commentId}`).classList.add('hidden');
    document.getElementById(`comment-actions-${commentId}`).classList.remove('hidden');

}

// 댓글 수정
function updateComment(commentId) {
    const editContent = document.getElementById(`edit-content-${commentId}`).value.trim();

    if (!editContent) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    const data = {
        commentContent: editContent
    };

    $.ajax({
        url: `/api/comments/${commentId}`,
        method: 'PUT',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (response) {
            if (response.success) {
                alert('댓글이 수정되었습니다.');
                loadComments();
            } else {
                alert('수정 실패: ' + response.message);
            }
        },
        error: function (xhr, status, error) {
            alert('수정 실패: ' + error);
        }
    });
}

// 댓글 삭제
function deleteComment(commentId) {
    if (!confirm('정말로 댓글을 삭제하시겠습니까?')) {
        return;
    }

    $.ajax({
        url: `/api/comments/${commentId}`,
        method: 'DELETE',
        success: function (response) {
            if (response.success) {
                alert('댓글이 삭제되었습니다.');
                loadComments();
                loadCommentCount();
            } else {
                alert('삭제 실패: ' + response.message);
            }
        },
        error: function (xhr, status, error) {
            alert('삭제 실패: ' + error);
        }
    });
}

// 댓글 개수 조회
function loadCommentCount() {
    const postId = document.getElementById('postId').value;

    $.ajax({
        url: `/api/comments/count/${postId}`,
        method: 'GET',
        success: function (response) {
            document.getElementById('commentCount').textContent = response.count;
        },
        error: function (xhr, status, error) {
            console.error('댓글 개수 조회 실패:', error);
        }
    });
}

//댓글 글자수 세기
document.getElementById('commentContent').addEventListener('input', function() {
    document.getElementById('charCount').textContent = this.value.length;
});



// 비회원 댓글 수정 완료
function updateGuestComment(commentId) {
    const editContent = document.getElementById(`edit-content-${commentId}`).value.trim();
    const password = document.getElementById(`edit-password-${commentId}`).value.trim();

    if (!password) {
        alert('비밀번호를 입력해주세요.');
        return;
    }

    if (!editContent) {
        alert('댓글 내용을 입력해주세요.');
        return;
    }

    const requestData = {
        commentContent: editContent,
        guestPassword: password,
        isGuest: true
    };

/*
    // CSRF 토큰 가져오기
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
*/


    fetch(`/api/comments/${commentId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json',
      /*      [header]: token  // CSRF 헤더 추가*/
        },
        body: JSON.stringify(requestData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('댓글이 수정되었습니다.');
                loadComments();
                loadCommentCount();
            } else {
                alert('수정 실패: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('댓글 수정 중 오류가 발생했습니다.');
        });
}

// 비회원 댓글 삭제 (확인창과 비밀번호 입력)
function deleteGuestComment(commentId) {
    if (!confirm('정말로 댓글을 삭제하시겠습니까?')) {
        return;
    }

    const password = prompt('댓글 비밀번호를 입력하세요:');
    if (!password) return;

/*
    // CSRF 토큰 가져오기
    const token = document.querySelector("meta[name='_csrf']").getAttribute("content");
    const header = document.querySelector("meta[name='_csrf_header']").getAttribute("content");
*/


    fetch(`/api/comments/guest/${commentId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json',
         /*   [header]: token  // CSRF 헤더 추가*/
        },
        body: JSON.stringify({ password: password })
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert('댓글이 삭제되었습니다.');
                loadComments();
                loadCommentCount();
            } else {
                alert('삭제 실패: ' + data.message);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('댓글 삭제 중 오류가 발생했습니다.');
        });
}



