% programa za fit na mono dava greshkata

function e = error_sa_ta(par)

global t tac tdo Xd Sd Ad F V Mmax K1 K Ks N K2 E K3 Kla DOo e


 Mmax = par(1);  Ks = par(2); K1 = par(3); 
% Mmax = par(1);  Ks = par(2); K1 = par(3); N = par(4);
% Mmax = par(1);  K = par(2); K1 = par(3); 

% K2 = par;
% K3 = par(1);  Kla = par(2); DOo = par(3);

options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t(1) t(length(t))];

 [T, X, Y1, Y2] = sim('Model_XS1',  TIMESPAN, options, []); % model 1
% [T, X, Y1, Y2] = sim('model_xs2',  TIMESPAN, options, []); % model 2
% [T, X, Y1, Y2] = sim('model_xs3',  TIMESPAN, options, []); % model 3

% [T, X, Y1, Y2, Y3] = sim('model_xsa1',  TIMESPAN, options, []); % model 1
% [T, X, Y1, Y2, Y3] = sim('model_xsa2',  TIMESPAN, options, []); % model 2
% [T, X, Y1, Y2, Y3] = sim('model_xsa3',  TIMESPAN, options, []); % model 3

% [T, X, Y1, Y2, Y3, Y4] = sim('model_xsao1',  TIMESPAN, options, []); % model 1
% [T, X, Y1, Y2, Y3, Y4] = sim('model_xsao2',  TIMESPAN, options, []); % model 2
% [T, X, Y1, Y2, Y3, Y4] = sim('model_xsao3',  TIMESPAN, options, []); % model 3

tmod = T; Xmod = Y1; Smod = Y2; 
% Amod = Y3; 
% DOdmod = Y4;

Xmod = interp1(tmod, Xmod, t); Smod = interp1(tmod, Smod, t);
% Amod = interp1(tmod, Amod, tac); 
% DOdmod = interp1(tmod, DODmod, tdo);

% error
e1 = sum(((Xd-Xmod).^2));
e2 = sum(((Sd-Smod).^2));
e = e1+e2;
% e3 = sum(((Ad-Amod).^2));
% e = e3;
% e4 = sum((DOd-DOdmod).^2));
% e = e4;




