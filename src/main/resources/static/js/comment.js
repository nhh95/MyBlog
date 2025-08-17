// 페이지 로드 시 댓글 목록 불러오기
$(document).ready(function() {
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

    console.log('현재 사용자 이메일:', currentUserEmail); // 디버깅용
    console.log('댓글 목록:', comments); // 디버깅용


    if (comments.length === 0) {
        commentList.innerHTML = '<p>등록된 댓글이 없습니다.</p>';
        return;
    }

    let html = '';
    comments.forEach(comment => {
        // 현재 사용자가 댓글 작성자인지 확인 (이메일 기준)
        const isOwner = currentUserEmail === comment.userEmail;

        console.log(`댓글 ID: ${comment.id}, 댓글 작성자: ${comment.userEmail}, 소유자 여부: ${isOwner}`);


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
                    <div class="comment-edit-form hidden" id="edit-form-${comment.id}">
                        <textarea id="edit-content-${comment.id}" class="comment-edit-textarea">${comment.commentContent}</textarea>
                        <button onclick="updateComment(${comment.id})" class="comment-edit-btn">수정 완료</button>
                        <button onclick="cancelEdit(${comment.id})" class="comment-cancel-btn">취소</button>
                    </div>
                    <div class="comment-actions" id="comment-actions-${comment.id}">
                        ${isOwner ? `
                            <button onclick="editComment(${comment.id})" class="comment-action-btn">수정</button>
                            <button onclick="deleteComment(${comment.id})" class="comment-action-btn">삭제</button>
                        ` : '<span class="comment-no-permission">수정/삭제 권한이 없습니다.</span>'}
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