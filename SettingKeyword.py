# 필요한 라이브러리 설치
# pip install sentence-transformers scikit-learn openai matplotlib sqlalchemy pymysql pandas
from sentence_transformers import SentenceTransformer
from sklearn.cluster import KMeans
from sklearn.metrics import silhouette_score
import openai
import matplotlib.pyplot as plt
from sqlalchemy import create_engine, text
import pandas as pd
import os

openai.api_key = os.getenv("OPENAI_KEY")
# -------------------------
# DB 연결 (SQLAlchemy 사용)
# -------------------------
DB_HOST = "ibridge-rds-instance.c1462ycqyo2o.ap-northeast-2.rds.amazonaws.com"
DB_PORT = 3306
DB_USER = "root"
DB_NAME = "ibridge"
DB_PASSWORD = os.environ.get("DB_PASSWORD")

# SQLAlchemy 엔진 생성 (pymysql 사용)
engine = create_engine(
    f"mysql+pymysql://{DB_USER}:{DB_PASSWORD}@{DB_HOST}:{DB_PORT}/{DB_NAME}",
    echo=False,
    future=True
)

# -------------------------
# BERT 임베딩 모델
# -------------------------
model = SentenceTransformer('sentence-t5-base')

# -------------------------
# GPT 키워드 추출 함수 (원본 그대로 둠)
# -------------------------
def extract_keywords(text_list):
    prompt = f"""
다음 문장들의 의미를 모두 담고 있을 만한 포괄적인 단어를 큰 따옴표 안에 묶어서 하나만 만들어줘. 예를 들어, "문화체험", "일상체험" 이런식이야 
문장 목록: {text_list}
"""
    response = openai.chat.completions.create(
        model="gpt-4.1-nano",
        messages=[{"role": "user", "content": prompt}],
        temperature=0.5,
        max_tokens=150
    )
    return response.choices[0].message.content

# -------------------------
# child별 subject 처리
# -------------------------
def process_child(child_id):
    # DB에서 subject 불러오기
    df_subjects = pd.read_sql(
        f"SELECT id, title FROM subject WHERE child = {child_id}", engine
    )

    if df_subjects.empty:
        print(f"child_id={child_id} → 데이터 없음")
        return

    sentences = df_subjects['title'].tolist()
    embeddings = model.encode(sentences)

    # -------------------------
    # 최적 k 찾기
    # -------------------------
    if len(sentences) > 1:
        range_n_clusters = range(2, min(10, len(sentences)))
        silhouette_scores = []

        for n_clusters in range_n_clusters:
            kmeans = KMeans(n_clusters=n_clusters, random_state=42)
            cluster_labels = kmeans.fit_predict(embeddings)
            score = silhouette_score(embeddings, cluster_labels)
            silhouette_scores.append(score)

        best_k = range_n_clusters[silhouette_scores.index(max(silhouette_scores))]
        print("최적 군집 수(k):", best_k)

        # -------------------------
        # KMeans로 군집화
        # -------------------------
        kmeans = KMeans(n_clusters=best_k, random_state=42)
        labels = kmeans.fit_predict(embeddings)

        clusters = {}
        for label, sentence, sid in zip(labels, sentences, df_subjects['id']):
            clusters.setdefault(label, []).append((sentence, sid))

        # -------------------------
        # 각 군집별 키워드 추출 + DB 업데이트
        # -------------------------
        with engine.begin() as conn:
            for cluster_id, cluster_data in clusters.items():
                cluster_sentences = [s[0] for s in cluster_data]
                cluster_ids = [s[1] for s in cluster_data]

                print(f"--- Cluster {cluster_id} ---")
                for s in cluster_sentences:
                    print(" -", s)

                keywords = extract_keywords(cluster_sentences)
                print("대표 키워드:", keywords, "\n")

                for sid in cluster_ids:
                    conn.execute(
                        text("UPDATE subject SET keyword = :keyword WHERE id = :id"),
                        {"keyword": keywords, "id": sid}
                    )
    else:
        # 문장이 1개일 때 → 바로 키워드 추출
        keywords = extract_keywords(sentences)
        sid = df_subjects['id'].iloc[0]
        with engine.begin() as conn:
            conn.execute(
                text("UPDATE subject SET keyword = :keyword WHERE id = :id"),
                {"keyword": keywords, "id": sid}
            )
        print(f"child_id={child_id}, subject_id={sid} → 키워드={keywords}")

# -------------------------
# 전체 실행
# -------------------------
if __name__ == "__main__":
    child_ids = pd.read_sql("SELECT DISTINCT child FROM subject", engine)['child_id'].tolist()
    for child_id in child_ids:
        process_child(child_id)
