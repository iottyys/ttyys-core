# -*-coding:utf-8-*-

import jieba
import os
import time
from gensim.corpora import dictionary
from gensim.models import tfidfmodel
from gensim.similarities import docsim
from simhash import Simhash


def send(message):
    print("the folder_path : ", message['message']['folder_path'])
    print("the stop_word_file : ", message['message']['stop_word_file'])
    print("the user_dict : ", message['message']['user_dict'])
    print("the cos_result_file : ", message['message']['cos_result_file'])
    print("the sim_result_file : ", message['message']['sim_result_file'])
    similarity(message['message']['folder_path'], message['message']['stop_word_file'], message['message']['user_dict'],
               message['message']['cos_result_file'], message['message']['sim_result_file'])
    return 'the result generate ok, please use new result...'


def similarity(folder_path, stop_word_file, user_dict, cos_result_file, sim_result_file):
    print("start time is :", time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))
    files = load_files(folder_path)
    words = [cut(file, stop_word_file, user_dict) for file in files]

    # 生成字典和向量语料
    doc_dict = dictionary.Dictionary(words)
    doc_corpus = [doc_dict.doc2bow(item) for item in words]

    # 5.通过token2id得到特征数（字典里面的键的个数）
    tfidf, index = tfidf_calc(doc_corpus, len(doc_dict.token2id.keys()))
    log = idx_out(files, index)

    # sims = simhash(words, doc_corpus)
    sims = simhash_tfidf(words, tfidf)
    log1 = sim_out(files, sims)

    write_file(cos_result_file, log)
    write_file(sim_result_file, log1)
    print("end time is :", time.strftime("%Y-%m-%d %H:%M:%S", time.localtime()))


def load_files(folder_path):
    if not os.path.exists(folder_path):
        raise Exception("folder must exists. ")
    files = []
    for file in os.listdir(folder_path):
        path = os.path.join(folder_path, file)
        if os.path.isdir(path):
            if path.endswith('.similarity'):
                continue
            else:
                files.extend(load_files(path))
        else:
            if path.endswith('.DS_Store'):
                continue
            else:
                files.append(path)
    return files


def cut(doc_file, stop_word_file, user_dict):
    if os.path.exists(user_dict):
        jieba.load_userdict(user_dict)
    segmented_words = jieba.lcut('，'.join([line.strip() for line in open(doc_file, 'r', encoding='utf-8').readlines()]))
    # 清理停用词
    if os.path.exists(stop_word_file):
        stopwords = [line.replace('\n', '') for line in open(stop_word_file, 'r', encoding='utf-8').readlines()]
        segmented_words = [i for i in segmented_words if i not in stopwords]

    # print(segmented_words)
    return segmented_words


def tfidf_calc(doc_corpus, num_features):
    tfidf_model = tfidfmodel.TfidfModel(doc_corpus)
    tfidf = tfidf_model[doc_corpus]
    index = docsim.Similarity('', tfidf, num_features=num_features)
    return tfidf, index[tfidf]


def simhash(words, doc_corpus):
    sims = []
    ws = []
    # doc_corpus 中向量索引与词的对应关系处理
    [ws.extend(sorted(set([w1 for w1 in w if w1 not in ws]))) for w in words]
    for word, corpus in zip(words, doc_corpus):
        feature = [(ws[c[0]], c[1]) for c in corpus]
        sim = Simhash(word)
        sim.build_by_features(feature)
        sims.append(sim)
    return sims


def simhash_tfidf(words, tfidfs):
    sims = []
    ws = []
    # doc_corpus 中向量索引与词的对应关系处理
    [ws.extend(sorted(set([w1 for w1 in w if w1 not in ws]))) for w in words]
    for word, tfidf in zip(words, tfidfs):
        feature = [(ws[c[0]], c[1]) for c in tfidf]
        sim = Simhash(word)
        sim.build_by_features(feature)
        sims.append(sim)
    return sims


def sim_out(files, sims):
    # log = 'simhash ==>>\n'
    log = ''
    for i, sim in enumerate(sims):
        for j in range(i + 1, len(sims)):
            if files[i] == files[j]:
                continue
            # str1 = '% 20s  %s  % 20s' % (files[i], '<==>', files[j])
            str1 = '%20s %s %20s' % (os.path.basename(files[i]), ':', os.path.basename(files[j]))
            if len(str1) < 52:
                str1 += (' ' * (52 - len(str1)))
            dis = sim.distance(sims[j])
            rate = (100.0 * (dis - 3) / 61) if dis > 3 else 100.0
            # log += (str1 + '[distance]:' + str(dis) + '[rate]: %.8f' % rate + '\n')
            log += (str1 + '- %.8f' % rate + '\n')
        log += '\n'
    return log


def idx_out(files, idx):
    log = ''
    for i, row in enumerate(idx):
        for file, item in zip(files, row):
            if files[i] == file:
                continue
            # if item == 0:
            # 相似度为0的
            # continue
            # str = '% 20s  %s  % 20s' % (files[i], ':', file)
            str = '% 20s  %s  % 20s' % (os.path.basename(files[i]), ':', os.path.basename(file))
            if len(str) < 52:
                str += (' ' * (52 - len(str)))
            # log += (str + '[similarity]: %12s' % item + '[rate]: %.8f' % (item * 100.0) + '\n')
            log += (str + '- %.8f' % (item * 100.0) + '\n')
        log += '\n'
    print(log)
    return log


def write_file(file_path, log):
    file = open(file_path, 'w', encoding='utf-8')
    file.write(log)
    file.close()


if __name__ == '__main__':
    similarity("/Volumes/works/tmp/text",
               "/Volumes/works/tmp/text/.similarity/stop_word",
               "/Volumes/works/tmp/text/.similarity/user_dict",
               '/Volumes/works/tmp/text/.similarity/cos_result',
               '/Volumes/works/tmp/text/.similarity/sim_result',
               )

