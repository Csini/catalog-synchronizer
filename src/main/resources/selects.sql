select requestid, count(*) from PRODUCT group by requestid;
select * from RUN;
select * from PRODUCT where requestid ="246cc035-e1fb-43bd-a5d3-59f9e7382a3e";
select * from PRODUCT where created is not null