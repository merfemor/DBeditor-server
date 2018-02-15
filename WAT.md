# Ebean

- empty pages in documentation (http://ebean-orm.github.io/docs/mapping/jpa/manyToMany, http://ebean-orm.github.io/docs/mapping/jpa/lob)
- @Id is always @GeneratedId
- class can't have many @Id (only as @Embeddable)
- EbeanException: null
- can't use ALTER. Each small modification of model is DROP all tables, then CREATE
- doen't use quotes in identificators, e.g. table for class User overrides PostgreSQL table "user".

## WAT Queries
- select **t0.creator_id**, t0.id, **t0.creator_id**, t0.dbms from database t0 where (t0.creator_id) in (0) ;
- select t0.id, t0.url, t0.dbms, t0.creator_id from database t0 where **t0.id in (0,1,0,0,0)** ;
