
document.addEventListener('DOMContentLoaded', function() {
    generateTableOfContents();
});

function generateTableOfContents() {
    // post-content ID와 portfolio-content 클래스 모두 찾기
    const contentElements = [
        document.getElementById('post-content'),
        document.querySelector('.portfolio-content')
    ].filter(element => element !== null);

    if (contentElements.length === 0) return;

    contentElements.forEach(contentElement => {
        const headings = contentElement.querySelectorAll('h1, h2, h3, h4, h5, h6');

        if (headings.length === 0) return;

        const tocContainer = document.createElement('div');
        tocContainer.className = 'toc-container';

        const tocTitle = document.createElement('h3');
        tocTitle.textContent = '목차';
        tocTitle.className = 'toc-title';

        const tocList = document.createElement('ul');
        tocList.className = 'toc-list';

        // 계층 구조를 위한 스택
        const levelStack = [];
        let currentList = tocList;

        headings.forEach((heading, index) => {
            const headingId = `heading-${Date.now()}-${index}`;
            heading.id = headingId;

            const currentLevel = parseInt(heading.tagName.substring(1));

            // 현재 레벨보다 깊은 레벨들을 스택에서 제거
            while (levelStack.length > 0 && levelStack[levelStack.length - 1].level >= currentLevel) {
                levelStack.pop();
            }

            // 현재 리스트 결정
            if (levelStack.length === 0) {
                currentList = tocList;
            } else {
                currentList = levelStack[levelStack.length - 1].list;
            }

            const listItem = document.createElement('li');
            const link = document.createElement('a');

            link.href = `#${headingId}`;
            link.textContent = heading.textContent;
            link.className = `toc-level-${heading.tagName.toLowerCase()}`;

            link.addEventListener('click', function(e) {
                e.preventDefault();
                const target = document.getElementById(headingId);
                if (target) {
                    const headerHeight = 64;
                    const targetPosition = target.offsetTop - headerHeight - 20;

                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });
                }
            });

            listItem.appendChild(link);
            currentList.appendChild(listItem);

            // 하위 레벨을 위한 새 ul 생성
            const nestedList = document.createElement('ul');
            nestedList.className = 'toc-list';
            listItem.appendChild(nestedList);

            // 스택에 현재 레벨 정보 추가
            levelStack.push({
                level: currentLevel,
                list: nestedList
            });
        });

        tocContainer.appendChild(tocTitle);
        tocContainer.appendChild(tocList);

        contentElement.parentNode.insertBefore(tocContainer, contentElement);
    });
}