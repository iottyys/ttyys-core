# -*-coding:utf-8-*-

import os

import jieba
from gensim.corpora import dictionary
from gensim.models import tfidfmodel


def send(message):
    return 'response: ' + str(message)


def similarity(doc_dir, corpus_file, stop_word_file, user_dict):
    if not os.path.exists(doc_dir) or os.path.exists(corpus_file):
        raise Exception("file of doc and corpus must exists. ")
    words = cut(doc_file, stop_word_file, user_dict)
    doc_corpus = corpus(words)
    tfidf(doc_corpus, corpus_file)


def cut(doc_file, stop_word_file, user_dict):
    if os.path.exists(user_dict):
        jieba.load_userdict(user_dict)

    segmented_words = jieba.lcut(open(doc_file, 'r', encoding='utf-8'))

    if os.path.exists(stop_word_file):
        stopwords = [line.strip() for line in open(stop_word_file, 'r', encoding='utf-8').readlines()]
        for word in segmented_words:
            if word in stopwords:
                segmented_words.remove(word)

    return segmented_words


def corpus(words):
    doc_dict = dictionary.Dictionary(words)
    doc_corpus = doc_dict.doc2bow(words)
    return doc_corpus


def tfidf(doc_corpus, corpus_file):
    tfidf_model = tfidfmodel.TfidfModel.load(corpus_file)
    tfidf_model


def cosdis():
    pass

